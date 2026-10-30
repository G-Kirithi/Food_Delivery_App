package com.food_delivery.cart.service;

import com.food_delivery.cart.dto.AddToCartRequest;
import com.food_delivery.cart.exception.CartNotFoundException;
import com.food_delivery.cart.exception.InvalidQuantityException;
import com.food_delivery.cart.exception.MenuItemUnavailableException;
import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.model.CartItem;
import com.food_delivery.cart.repository.CartRepository;
import com.food_delivery.customer.exception.CustomerNotFoundException;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.restaurant.exception.ValidationException;
import com.food_delivery.restaurant.model.MenuItem;
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.RestaurantService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementation of CartService coordinating cart lifecycle, item manipulation,
 * and integration with Module 1 (Restaurant & Menu Management).
 */
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public CartServiceImpl(CartRepository cartRepository,
                           CustomerRepository customerRepository,
                           RestaurantService restaurantService,
                           MenuService menuService) {
        if (cartRepository == null) {
            throw new IllegalArgumentException("CartRepository cannot be null.");
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
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    @Override
    public Cart addItemToCart(AddToCartRequest request) {
        if (request == null) {
            throw new ValidationException("AddToCartRequest cannot be null.");
        }
        return addItemToCart(request.getCustomerId(), request.getRestaurantId(), request.getMenuItemId(), request.getQuantity());
    }

    @Override
    public Cart addItemToCart(String customerId, String restaurantId, String menuItemId, int quantity) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
        if (menuItemId == null || menuItemId.trim().isEmpty()) {
            throw new ValidationException("Menu item ID cannot be null or blank.");
        }
        if (quantity < CartItem.MIN_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Quantity must be greater than zero. Provided: " + quantity);
        }
        if (quantity > CartItem.MAX_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Maximum " + CartItem.MAX_ITEM_QUANTITY + " units of the same menu item can be added. Provided: " + quantity);
        }

        String cleanCustId = customerId.trim();
        String cleanRestId = restaurantId.trim();
        String cleanItemId = menuItemId.trim();

        // 1. Verify customer exists
        if (!customerRepository.existsById(cleanCustId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + cleanCustId);
        }

        // 2. Verify restaurant exists in Module 1
        restaurantService.getRestaurantById(cleanRestId);

        // 3. Verify menu item exists in Module 1
        MenuItem menuItem = menuService.getMenuItemById(cleanItemId);

        // 4. Verify menu item belongs to the specified restaurant
        if (!menuItem.getRestaurantId().equals(cleanRestId)) {
            throw new ValidationException("Menu item '" + menuItem.getName() + "' (ID: " + cleanItemId +
                    ") does not belong to restaurant ID: " + cleanRestId);
        }

        // 5. Verify menu item availability
        if (!menuItem.isAvailable() || !menuService.isAvailable(cleanItemId)) {
            throw new MenuItemUnavailableException("Menu item '" + menuItem.getName() + "' (ID: " + cleanItemId + ") is currently unavailable.");
        }

        // 6. Verify price > 0
        BigDecimal price = menuItem.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Menu item price must be greater than zero. Found: " + price);
        }

        // 7. Get or create active cart for customer
        Cart cart = getOrCreateCart(cleanCustId);

        // 8. Construct CartItem
        String cartItemId = "CI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CartItem cartItem = new CartItem(cartItemId, menuItem.getId(), menuItem.getName(), price, quantity);

        // 9. Add item into cart (enforces single-restaurant and max-10 rules)
        cart.addItem(cleanRestId, cartItem);

        return cartRepository.save(cart);
    }

    @Override
    public Cart getOrCreateCart(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
        String cleanCustId = customerId.trim();
        if (!customerRepository.existsById(cleanCustId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + cleanCustId);
        }

        return cartRepository.findByCustomerId(cleanCustId)
                .orElseGet(() -> {
                    String cartId = "CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    Cart newCart = new Cart(cartId, cleanCustId);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Cart getCartByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
        String cleanCustId = customerId.trim();
        if (!customerRepository.existsById(cleanCustId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + cleanCustId);
        }
        return cartRepository.findByCustomerId(cleanCustId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + cleanCustId));
    }

    @Override
    public Cart getCartById(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new ValidationException("Cart ID cannot be null or blank.");
        }
        return cartRepository.findById(cartId.trim())
                .orElseThrow(() -> new CartNotFoundException("Cart not found with ID: " + cartId));
    }

    @Override
    public Cart updateCartItemQuantity(String customerId, String cartItemId, int newQuantity) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
        if (newQuantity < CartItem.MIN_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Quantity must be greater than zero. Provided: " + newQuantity);
        }
        if (newQuantity > CartItem.MAX_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Maximum " + CartItem.MAX_ITEM_QUANTITY + " units of the same menu item can be added. Provided: " + newQuantity);
        }

        Cart cart = getCartByCustomerId(customerId);
        cart.updateItemQuantity(cartItemId, newQuantity);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeCartItem(String customerId, String cartItemId) {
        Cart cart = getCartByCustomerId(customerId);
        cart.removeItem(cartItemId);
        return cartRepository.save(cart);
    }

    @Override
    public Cart clearCart(String customerId) {
        Cart cart = getCartByCustomerId(customerId);
        cart.clear();
        return cartRepository.save(cart);
    }
}
