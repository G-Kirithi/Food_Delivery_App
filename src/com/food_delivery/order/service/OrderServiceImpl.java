package com.food_delivery.order.service;

import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.model.CartItem;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.order.dto.PlaceOrderRequest;
import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.exception.OrderNotFoundException;
import com.food_delivery.order.exception.OrderTimeoutException;
import com.food_delivery.order.exception.OrderValidationException;
import com.food_delivery.order.exception.UnauthorizedOrderException;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;
import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.model.Restaurant;
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.RestaurantService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of OrderService coordinating order validation, snapshot creation,
 * cart clearing, strict lifecycle transitions, and timeout monitoring.
 */
public class OrderServiceImpl implements OrderService {

    public static final int MAX_ACTIVE_ORDERS_PER_CUSTOMER = 3;
    public static final long ACCEPTANCE_TIMEOUT_MINUTES = 5;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            RestaurantService restaurantService,
                            MenuService menuService,
                            CartService cartService) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null.");
        }
        if (customerRepository == null) {
            throw new IllegalArgumentException("CustomerRepository cannot be null.");
        }
        if (restaurantService == null) {
            throw new IllegalArgumentException("RestaurantService cannot be null.");
        }
        if (menuService == null) {
            throw new IllegalArgumentException("MenuService cannot be null.");
        }
        if (cartService == null) {
            throw new IllegalArgumentException("CartService cannot be null.");
        }

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.cartService = cartService;
    }

    @Override
    public Order placeOrder(PlaceOrderRequest request) {
        if (request == null) {
            throw new OrderValidationException("PlaceOrderRequest cannot be null.");
        }
        return placeOrder(request.getCustomerId(), request.getDeliveryAddressId(), null);
    }

    @Override
    public Order placeOrder(String customerId, String deliveryAddressId) {
        return placeOrder(customerId, deliveryAddressId, null);
    }

    @Override
    public Order placeOrder(String customerId, String deliveryAddressId, LocalTime evaluationTime) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new OrderValidationException("Customer ID cannot be null or blank.");
        }
        if (deliveryAddressId == null || deliveryAddressId.trim().isEmpty()) {
            throw new OrderValidationException("Delivery address ID cannot be null or blank.");
        }

        String cleanCustId = customerId.trim();
        String cleanAddressId = deliveryAddressId.trim();

        // 1. Verify customer exists
        Customer customer = customerRepository.findById(cleanCustId)
                .orElseThrow(() -> new OrderValidationException("Customer not found with ID: " + cleanCustId));

        // 2. Verify selected delivery address exists
        Address deliveryAddress = customer.getAddressById(cleanAddressId)
                .orElseThrow(() -> new OrderValidationException("Delivery address ID '" + cleanAddressId + "' does not belong to customer '" + cleanCustId + "'."));

        // 3. Enforce maximum 3 active orders per customer rule
        int activeOrderCount = orderRepository.countActiveOrdersByCustomerId(cleanCustId);
        if (activeOrderCount >= MAX_ACTIVE_ORDERS_PER_CUSTOMER) {
            throw new OrderValidationException(
                    "Customer has reached the maximum allowed limit of " + MAX_ACTIVE_ORDERS_PER_CUSTOMER +
                    " active orders. Please wait for an existing order to complete or cancel it before ordering again."
            );
        }

        // 4. Retrieve customer's active cart
        Cart cart;
        try {
            cart = cartService.getCartByCustomerId(cleanCustId);
        } catch (com.food_delivery.cart.exception.CartNotFoundException e) {
            throw new OrderValidationException("Cannot place order with an empty shopping cart.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new OrderValidationException("Cannot place an order with an empty shopping cart.");
        }

        String restaurantId = cart.getRestaurantId();
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new OrderValidationException("Shopping cart does not have an associated restaurant.");
        }

        // 5. Verify restaurant exists
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);

        // 6. Enforce delivery address city matches restaurant city
        if (!deliveryAddress.getCity().trim().equalsIgnoreCase(restaurant.getCity().trim())) {
            throw new OrderValidationException(
                    "The delivery address city ('" + deliveryAddress.getCity() +
                    "') must match the restaurant's city ('" + restaurant.getCity() +
                    "'). Inter-city delivery is not serviceable."
            );
        }

        // 7. Verify restaurant is open and accepting orders
        LocalTime timeToCheck = (evaluationTime != null) ? evaluationTime : LocalTime.now();
        if (!restaurant.isAcceptingOrders(timeToCheck)) {
            throw new OrderValidationException(
                    "Restaurant '" + restaurant.getName() + "' is currently closed or not accepting orders at " +
                    timeToCheck + ". (Operating hours: " + restaurant.getOpeningTime() + " - " + restaurant.getClosingTime() +
                    ", Open status: " + restaurant.isOpen() + ")"
            );
        }

        // 8. Enforce item availability & checkout removal rule
        // "A menu item marked Unavailable cannot be added to a cart. If an item becomes unavailable after it was added to a cart,
        // it is removed at checkout and the customer is notified."
        List<String> unavailableItemNames = new ArrayList<>();
        List<CartItem> cartItemsSnapshot = new ArrayList<>(cart.getItems());

        for (CartItem item : cartItemsSnapshot) {
            boolean isStillAvailable = menuService.isAvailable(item.getMenuItemId());
            if (!isStillAvailable) {
                unavailableItemNames.add(item.getItemName());
                cartService.removeCartItem(cleanCustId, item.getId());
            }
        }

        if (!unavailableItemNames.isEmpty()) {
            throw new OrderValidationException(
                    "Item(s) became unavailable and were removed from your cart at checkout: " +
                    String.join(", ", unavailableItemNames) + ". Please review your cart before placing the order."
            );
        }

        // 9. Enforce minimum order value
        BigDecimal itemsTotal = cart.getTotalAmount();
        if (itemsTotal.compareTo(restaurant.getMinimumOrderValue()) < 0) {
            throw new OrderValidationException(
                    "Order total of Rs. " + itemsTotal + " does not meet restaurant minimum order value of Rs. " +
                    restaurant.getMinimumOrderValue() + "."
            );
        }

        // 10. Capture frozen OrderItem snapshots (price cannot change later)
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            orderItems.add(new OrderItem(ci.getMenuItemId(), ci.getItemName(), ci.getItemPrice(), ci.getQuantity()));
        }

        // 11. Create Order entity
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String fullAddressText = formatAddress(deliveryAddress);

        Order order = new Order(
                orderId,
                customer.getId(),
                customer.getFullName(),
                restaurant.getId(),
                restaurant.getName(),
                deliveryAddress.getId(),
                deliveryAddress.getCity(),
                fullAddressText,
                orderItems,
                itemsTotal,
                Instant.now()
        );

        // 12. Save order
        orderRepository.save(order);

        // 13. Clear cart once order is placed
        cartService.clearCart(cleanCustId);

        return order;
    }

    @Override
    public Order acceptOrder(String orderId, String restaurantId, UserRole role) {
        return acceptOrder(orderId, restaurantId, role, Instant.now());
    }

    @Override
    public Order acceptOrder(String orderId, String restaurantId, UserRole role, Instant acceptTime) {
        checkAuthorizedAdminOrOwner(role, "accept order");

        Order order = getOrderById(orderId);
        validateRestaurantOwnership(order, restaurantId, role);

        Instant evaluationInstant = (acceptTime != null) ? acceptTime : Instant.now();
        Duration elapsed = Duration.between(order.getPlacedAt(), evaluationInstant);

        // Enforce 5-minute restaurant acceptance rule:
        // "The restaurant must accept an order within 5 minutes of placement. Otherwise the order is automatically cancelled."
        if (elapsed.toMinutes() >= ACCEPTANCE_TIMEOUT_MINUTES) {
            order.autoCancel("Auto-cancelled: Restaurant did not accept within 5 minutes (elapsed: " + elapsed.toMinutes() + " mins).");
            orderRepository.save(order);
            throw new OrderTimeoutException(
                    "Order " + orderId + " has expired and was automatically cancelled because it was not accepted within 5 minutes (elapsed: " +
                    elapsed.toMinutes() + " mins)."
            );
        }

        order.transitionTo(OrderStatus.ACCEPTED);
        return orderRepository.save(order);
    }

    @Override
    public Order rejectOrder(String orderId, String restaurantId, String reason, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "reject order");

        Order order = getOrderById(orderId);
        validateRestaurantOwnership(order, restaurantId, role);

        // "A restaurant can reject an order only while it is in Placed status, and must give a reason."
        order.reject(reason);
        return orderRepository.save(order);
    }

    @Override
    public Order startPreparation(String orderId, String restaurantId, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "start order preparation");

        Order order = getOrderById(orderId);
        validateRestaurantOwnership(order, restaurantId, role);

        order.transitionTo(OrderStatus.PREPARING);
        return orderRepository.save(order);
    }

    @Override
    public Order markReadyForPickup(String orderId, String restaurantId, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "mark order ready for pickup");

        Order order = getOrderById(orderId);
        validateRestaurantOwnership(order, restaurantId, role);

        order.transitionTo(OrderStatus.READY_FOR_PICKUP);
        return orderRepository.save(order);
    }

    @Override
    public Order markOutForDelivery(String orderId, String deliveryPartnerId) {
        Order order = getOrderById(orderId);
        if (deliveryPartnerId != null) {
            order.setDeliveryPartnerId(deliveryPartnerId.trim());
        }
        order.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
        return orderRepository.save(order);
    }

    @Override
    public Order markDelivered(String orderId) {
        Order order = getOrderById(orderId);
        order.transitionTo(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(String orderId, String customerId, String reason) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new OrderValidationException("Customer ID cannot be null or blank for cancellation.");
        }

        Order order = getOrderById(orderId);
        if (!order.getCustomerId().equals(customerId.trim())) {
            throw new UnauthorizedOrderException("Customer ID '" + customerId + "' is not authorized to cancel order ID '" + orderId + "'.");
        }

        // "A customer can cancel an order only while it is in Placed or Accepted status. Cancellation is not allowed once preparation starts."
        order.cancelByCustomer(reason);
        return orderRepository.save(order);
    }

    @Override
    public int checkAndTriggerAutoCancellations() {
        List<Order> placedOrders = orderRepository.findByStatus(OrderStatus.PLACED);
        Instant now = Instant.now();
        int cancelledCount = 0;

        for (Order order : placedOrders) {
            Duration elapsed = Duration.between(order.getPlacedAt(), now);
            if (elapsed.toMinutes() >= ACCEPTANCE_TIMEOUT_MINUTES) {
                order.autoCancel("Auto-cancelled: Restaurant did not accept within 5 minutes.");
                orderRepository.save(order);
                cancelledCount++;
            }
        }

        return cancelledCount;
    }

    @Override
    public Order getOrderById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new OrderValidationException("Order ID cannot be null or blank.");
        }
        return orderRepository.findById(orderId.trim())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    @Override
    public List<Order> getOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new OrderValidationException("Customer ID cannot be null or blank.");
        }
        return orderRepository.findByCustomerId(customerId.trim());
    }

    @Override
    public List<Order> getOrdersByRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new OrderValidationException("Restaurant ID cannot be null or blank.");
        }
        return orderRepository.findByRestaurantId(restaurantId.trim());
    }

    @Override
    public List<Order> getActiveOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new OrderValidationException("Customer ID cannot be null or blank.");
        }
        return orderRepository.findActiveOrdersByCustomerId(customerId.trim());
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // --- Helper Validation Methods ---

    private void checkAuthorizedAdminOrOwner(UserRole role, String action) {
        if (role == null) {
            throw new UnauthorizedOrderException("User role cannot be null. Authorization failed to " + action + ".");
        }
        if (role == UserRole.CUSTOMER) {
            throw new UnauthorizedOrderException("User with role CUSTOMER is not authorized to " + action + ".");
        }
    }

    private void validateRestaurantOwnership(Order order, String restaurantId, UserRole role) {
        if (role == UserRole.RESTAURANT_OWNER) {
            if (restaurantId == null || !order.getRestaurantId().equals(restaurantId.trim())) {
                throw new UnauthorizedOrderException(
                        "Restaurant owner of '" + restaurantId + "' is not authorized to manage order for restaurant '" +
                        order.getRestaurantId() + "'."
                );
            }
        }
    }

    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getHouseDetails()).append(", ")
          .append(address.getStreet()).append(", ")
          .append(address.getArea()).append(", ")
          .append(address.getCity()).append(" - ")
          .append(address.getPincode());
        if (address.getLandmark() != null && !address.getLandmark().isEmpty()) {
            sb.append(" (Near ").append(address.getLandmark()).append(")");
        }
        return sb.toString();
    }
}
