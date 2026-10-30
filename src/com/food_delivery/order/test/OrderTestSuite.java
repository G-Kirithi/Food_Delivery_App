package com.food_delivery.order.test;

import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.repository.CartRepository;
import com.food_delivery.cart.repository.InMemoryCartRepository;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.cart.service.CartServiceImpl;
import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.customer.repository.InMemoryCustomerRepository;
import com.food_delivery.customer.service.CustomerService;
import com.food_delivery.customer.service.CustomerServiceImpl;
import com.food_delivery.order.controller.OrderController;
import com.food_delivery.order.dto.OrderResponse;
import com.food_delivery.order.dto.PlaceOrderRequest;
import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.exception.InvalidOrderStateException;
import com.food_delivery.order.exception.OrderTimeoutException;
import com.food_delivery.order.exception.OrderValidationException;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;
import com.food_delivery.order.repository.InMemoryOrderRepository;
import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.order.service.ActiveOrderCheckerImpl;
import com.food_delivery.order.service.OrderService;
import com.food_delivery.order.service.OrderServiceImpl;
import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.RestaurantStateException;
import com.food_delivery.restaurant.model.MenuItem;
import com.food_delivery.restaurant.model.Restaurant;
import com.food_delivery.restaurant.repository.InMemoryMenuItemRepository;
import com.food_delivery.restaurant.repository.InMemoryRestaurantRepository;
import com.food_delivery.restaurant.repository.MenuItemRepository;
import com.food_delivery.restaurant.repository.RestaurantRepository;
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.MenuServiceImpl;
import com.food_delivery.restaurant.service.RestaurantService;
import com.food_delivery.restaurant.service.RestaurantServiceImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic Unit and Integration Test Suite for Module 3 (Order Placement and Status).
 * Covers all 16 business rules and edge cases specified in the problem statement.
 */
public class OrderTestSuite {

    private RestaurantRepository restaurantRepo;
    private MenuItemRepository menuRepo;
    private RestaurantService restaurantService;
    private MenuService menuService;

    private CustomerRepository customerRepo;
    private CustomerService customerService;

    private CartRepository cartRepo;
    private CartService cartService;

    private OrderRepository orderRepo;
    private OrderService orderService;
    private OrderController orderController;

    private static final String REST_ID = "REST-MUM-01";
    private static final String CUST_ID = "CUST-MUM-01";
    private static final String ADDR_ID = "ADDR-MUM-01";
    private static final String ITEM_1 = "ITEM-PANEER";
    private static final String ITEM_2 = "ITEM-NAAN";

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) {
        OrderTestSuite suite = new OrderTestSuite();
        suite.runAllTests();
    }

    public void runAllTests() {
        System.out.println("================================================================================");
        System.out.println("        RUNNING FOOD DELIVERY APP - MODULE 3 TEST SUITE (16 TEST CASES)         ");
        System.out.println("================================================================================");

        test1_successfulOrderPlacement();
        test2_priceImmutabilitySnapshot();
        test3_rejectInterCityDelivery();
        test4_rejectWhenRestaurantClosed();
        test5_rejectWhenMinimumOrderValueNotMet();
        test6_unavailableItemAutoRemovedAtCheckout();
        test7_maxThreeActiveOrdersLimit();
        test8_rejectOrderWithEmptyCart();
        test9_validSequentialStateTransitions();
        test10_rejectIllegalStateTransitions();
        test11_customerCancellationInPlacedAndAccepted();
        test12_rejectCancellationAfterPreparationStarts();
        test13_restaurantRejectionOnlyInPlacedWithReason();
        test14_fiveMinuteAcceptanceTimeoutAutoCancellation();
        test15_activeOrderCheckerPreventsRestaurantDeletion();
        test16_orderControllerRestEndpoints();

        System.out.println("================================================================================");
        System.out.println("MODULE 3 TESTS: " + (passed + failed) + " | PASSED: " + passed + " | FAILED: " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            throw new AssertionError("Module 3 test suite encountered " + failed + " failures.");
        }
    }

    private void setUp() {
        restaurantRepo = new InMemoryRestaurantRepository();
        menuRepo = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantServiceImpl(restaurantRepo, menuRepo);
        menuService = new MenuServiceImpl(menuRepo, restaurantRepo);

        customerRepo = new InMemoryCustomerRepository();
        customerService = new CustomerServiceImpl(customerRepo);

        cartRepo = new InMemoryCartRepository();
        cartService = new CartServiceImpl(cartRepo, customerRepo, restaurantService, menuService);

        orderRepo = new InMemoryOrderRepository();
        orderService = new OrderServiceImpl(orderRepo, customerRepo, restaurantService, menuService, cartService);
        orderController = new OrderController(orderService);

        // Configure ActiveOrderChecker integration on RestaurantService
        restaurantService.setActiveOrderChecker(new ActiveOrderCheckerImpl(orderRepo));

        // Seed a valid active restaurant in Mumbai, open 09:00 - 23:00, min order Rs. 200
        Restaurant restaurant = new Restaurant(
                REST_ID, "Taj Mahal Spice", CuisineType.NORTH_INDIAN, "Mumbai",
                LocalTime.of(9, 0), LocalTime.of(23, 0), BigDecimal.valueOf(200.0)
        );
        restaurantService.addRestaurant(restaurant, UserRole.ADMIN);
        restaurantService.activateRestaurant(REST_ID, UserRole.ADMIN);
        restaurantService.markOpen(REST_ID, UserRole.ADMIN);

        // Seed menu items
        menuService.addMenuItem(new MenuItem(ITEM_1, REST_ID, "Paneer Butter Masala", BigDecimal.valueOf(250.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);
        menuService.addMenuItem(new MenuItem(ITEM_2, REST_ID, "Butter Naan", BigDecimal.valueOf(40.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);

        // Seed a valid customer in Mumbai with delivery address
        Address address = new Address(ADDR_ID, "Flat 402", "MG Road", "Bandra West", "Mumbai", "Maharashtra", "400050");
        Customer customer = new Customer(CUST_ID, "Aarav Sharma", "+919876543210", "aarav@example.com", Collections.singletonList(address));
        customerRepo.save(customer);
    }

    private void test1_successfulOrderPlacement() {
        setUp();
        try {
            // Add items to cart (Total: 250*1 + 40*2 = 330, > minOrder 200)
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_2, 2);

            // Place order at 12:00 PM (within operating hours)
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));

            assertEquals(OrderStatus.PLACED, order.getStatus(), "Status should be PLACED");
            assertEquals(BigDecimal.valueOf(330.0), order.getTotalAmount(), "Total amount should match");
            assertEquals(2, order.getItems().size(), "Order item count should be 2");
            assertEquals("Mumbai", order.getDeliveryCity(), "Delivery city should be Mumbai");

            // Verify cart was cleared
            Cart cart = cartService.getCartByCustomerId(CUST_ID);
            assertTrue(cart.isEmpty(), "Customer shopping cart must be cleared upon order placement");

            pass("Test 1: Successfully place an order with valid prerequisites and verify cart cleared");
        } catch (Exception e) {
            fail("Test 1: Successfully place an order", e);
        }
    }

    private void test2_priceImmutabilitySnapshot() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));

            OrderItem originalSnapshotItem = order.getItems().get(0);
            assertEquals(BigDecimal.valueOf(250.0), originalSnapshotItem.getItemPrice(), "Captured snapshot price should be 250");

            // Later menu price change in Module 1: increase Paneer from 250 to 350
            menuService.updateItemPrice(ITEM_1, BigDecimal.valueOf(350.0), UserRole.RESTAURANT_OWNER);

            // Fetch order again and verify its price remains exactly 250
            Order retrievedOrder = orderService.getOrderById(order.getId());
            OrderItem itemAfterMenuUpdate = retrievedOrder.getItems().get(0);

            assertEquals(BigDecimal.valueOf(250.0), itemAfterMenuUpdate.getItemPrice(),
                    "Order snapshot price must NOT be affected by later menu price updates");
            assertEquals(BigDecimal.valueOf(250.0), retrievedOrder.getTotalAmount(),
                    "Order total amount must remain unchanged after later menu price changes");

            pass("Test 2: Order item prices remain frozen and immune to subsequent menu price changes");
        } catch (Exception e) {
            fail("Test 2: Price immutability snapshot", e);
        }
    }

    private void test3_rejectInterCityDelivery() {
        setUp();
        try {
            // Customer adds a Pune delivery address directly
            Address puneAddress = new Address("ADDR-PUNE-01", "Villa 10", "FC Road", "Shivajinagar", "Pune", "Maharashtra", "411005");
            Customer customer = customerRepo.findById(CUST_ID).get();
            customer.addAddress(puneAddress);
            customerRepo.save(customer);

            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);

            // Attempt to order from Mumbai restaurant to Pune address
            orderService.placeOrder(CUST_ID, "ADDR-PUNE-01", LocalTime.of(12, 0));
            fail("Test 3: Reject inter-city delivery - Expected OrderValidationException not thrown");
        } catch (OrderValidationException e) {
            assertTrue(e.getMessage().contains("must match the restaurant's city"), "Error message should mention city mismatch");
            pass("Test 3: Reject order placement when delivery address city does not match restaurant city");
        } catch (Exception e) {
            fail("Test 3: Reject inter-city delivery", e);
        }
    }

    private void test4_rejectWhenRestaurantClosed() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);

            // Restaurant operates 09:00 - 23:00. Attempt placement at 03:00 AM (Closed)
            orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(3, 0));
            fail("Test 4: Reject when restaurant closed - Expected OrderValidationException not thrown");
        } catch (OrderValidationException e) {
            assertTrue(e.getMessage().contains("currently closed or not accepting orders"), "Error message should state closed");
            pass("Test 4: Reject order placement when restaurant is outside operating hours or closed");
        } catch (Exception e) {
            fail("Test 4: Reject when restaurant closed", e);
        }
    }

    private void test5_rejectWhenMinimumOrderValueNotMet() {
        setUp();
        try {
            // Only 1 Naan (Rs. 40 < min order Rs. 200)
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_2, 1);

            orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            fail("Test 5: Reject minimum order value - Expected OrderValidationException not thrown");
        } catch (OrderValidationException e) {
            assertTrue(e.getMessage().contains("does not meet restaurant minimum order value"), "Error should mention min order");
            pass("Test 5: Reject order placement when cart total is below restaurant minimum order value");
        } catch (Exception e) {
            fail("Test 5: Reject minimum order value", e);
        }
    }

    private void test6_unavailableItemAutoRemovedAtCheckout() {
        setUp();
        try {
            // Add Paneer and Naan to cart
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_2, 2);

            // Restaurant runs out of Paneer (mark unavailable)
            menuService.markItemUnavailable(ITEM_1, UserRole.RESTAURANT_OWNER);

            // Customer attempts to place order:
            // "If an item becomes unavailable after it was added to a cart, it is removed at checkout and the customer is notified."
            try {
                orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
                fail("Test 6: Unavailable item notification - Expected OrderValidationException not thrown");
            } catch (OrderValidationException e) {
                assertTrue(e.getMessage().contains("became unavailable and were removed from your cart at checkout"),
                        "Message should notify customer of item removal");
            }

            // Verify the unavailable item was indeed removed from cart, leaving only Naan
            Cart cart = cartService.getCartByCustomerId(CUST_ID);
            assertEquals(1, cart.getItems().size(), "Cart should only have 1 item left");
            assertEquals(ITEM_2, cart.getItems().get(0).getMenuItemId(), "Remaining item should be Naan");

            pass("Test 6: Unavailable menu items are removed at checkout and customer is notified");
        } catch (Exception e) {
            fail("Test 6: Unavailable item auto removed at checkout", e);
        }
    }

    private void test7_maxThreeActiveOrdersLimit() {
        setUp();
        try {
            // Place 3 active orders
            for (int i = 1; i <= 3; i++) {
                cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
                orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            }

            assertEquals(3, orderService.getActiveOrdersByCustomerId(CUST_ID).size(), "Should have exactly 3 active orders");

            // Attempt to place a 4th order
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            try {
                orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
                fail("Test 7: Max 3 active orders - Expected OrderValidationException not thrown");
            } catch (OrderValidationException e) {
                assertTrue(e.getMessage().contains("maximum allowed limit of 3 active orders"), "Message should mention limit");
            }

            pass("Test 7: Enforce customer maximum limit of 3 active orders at any time");
        } catch (Exception e) {
            fail("Test 7: Max 3 active orders limit", e);
        }
    }

    private void test8_rejectOrderWithEmptyCart() {
        setUp();
        try {
            orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            fail("Test 8: Reject empty cart - Expected OrderValidationException not thrown");
        } catch (OrderValidationException e) {
            assertTrue(e.getMessage().contains("empty shopping cart"), "Message should mention empty cart");
            pass("Test 8: Reject order placement when customer shopping cart is empty");
        } catch (Exception e) {
            fail("Test 8: Reject empty cart", e);
        }
    }

    private void test9_validSequentialStateTransitions() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            assertEquals(OrderStatus.PLACED, order.getStatus(), "Step 1: Status must be PLACED");

            // 1. PLACED -> ACCEPTED
            order = orderService.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(OrderStatus.ACCEPTED, order.getStatus(), "Step 2: Status must be ACCEPTED");
            assertNotNull(order.getAcceptedAt(), "Accepted timestamp must be recorded");

            // 2. ACCEPTED -> PREPARING
            order = orderService.startPreparation(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(OrderStatus.PREPARING, order.getStatus(), "Step 3: Status must be PREPARING");
            assertNotNull(order.getPreparingAt(), "Preparing timestamp must be recorded");

            // 3. PREPARING -> READY_FOR_PICKUP
            order = orderService.markReadyForPickup(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus(), "Step 4: Status must be READY_FOR_PICKUP");
            assertNotNull(order.getReadyForPickupAt(), "Ready for pickup timestamp must be recorded");

            // 4. READY_FOR_PICKUP -> OUT_FOR_DELIVERY
            order = orderService.markOutForDelivery(orderId, "DP-007");
            assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus(), "Step 5: Status must be OUT_FOR_DELIVERY");
            assertEquals("DP-007", order.getDeliveryPartnerId(), "Delivery partner must be recorded");

            // 5. OUT_FOR_DELIVERY -> DELIVERED
            order = orderService.markDelivered(orderId);
            assertEquals(OrderStatus.DELIVERED, order.getStatus(), "Step 6: Status must be DELIVERED");
            assertNotNull(order.getDeliveredAt(), "Delivered timestamp must be recorded");
            assertFalse(order.isActive(), "Delivered order must no longer be active");

            pass("Test 9: Successfully execute complete valid state transition lifecycle in sequential order");
        } catch (Exception e) {
            fail("Test 9: Valid sequential state transitions", e);
        }
    }

    private void test10_rejectIllegalStateTransitions() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            // Attempt to skip ACCEPTED and directly move from PLACED to PREPARING
            try {
                orderService.startPreparation(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
                fail("Test 10: Expected InvalidOrderStateException when skipping ACCEPTED");
            } catch (InvalidOrderStateException e) {
                assertTrue(e.getMessage().contains("Illegal order status transition"), "Should reject illegal skip");
            }

            // Accept properly
            orderService.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER);

            // Attempt to move backwards from ACCEPTED to PLACED
            try {
                order.transitionTo(OrderStatus.PLACED);
                fail("Test 10: Expected InvalidOrderStateException when moving backwards");
            } catch (InvalidOrderStateException e) {
                assertTrue(e.getMessage().contains("Illegal order status transition"), "Should reject backwards transition");
            }

            pass("Test 10: Reject illegal status transitions (skipping steps or moving backwards)");
        } catch (Exception e) {
            fail("Test 10: Reject illegal state transitions", e);
        }
    }

    private void test11_customerCancellationInPlacedAndAccepted() {
        setUp();
        try {
            // Case A: Cancel while in PLACED status
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order1 = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            Order cancelled1 = orderService.cancelOrder(order1.getId(), CUST_ID, "Changed my mind");

            assertEquals(OrderStatus.CANCELLED, cancelled1.getStatus(), "Order 1 should be CANCELLED");
            assertEquals("Changed my mind", cancelled1.getCancellationReason(), "Reason should be recorded");
            assertNotNull(cancelled1.getCancelledAt(), "Cancelled timestamp should be recorded");

            // Case B: Cancel while in ACCEPTED status
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order2 = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            orderService.acceptOrder(order2.getId(), REST_ID, UserRole.RESTAURANT_OWNER);
            Order cancelled2 = orderService.cancelOrder(order2.getId(), CUST_ID, "Need to leave home");

            assertEquals(OrderStatus.CANCELLED, cancelled2.getStatus(), "Order 2 should be CANCELLED");
            assertEquals("Need to leave home", cancelled2.getCancellationReason(), "Reason should be recorded");

            pass("Test 11: Customer can successfully cancel order in PLACED or ACCEPTED status");
        } catch (Exception e) {
            fail("Test 11: Customer cancellation in PLACED and ACCEPTED", e);
        }
    }

    private void test12_rejectCancellationAfterPreparationStarts() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            // Progress to PREPARING
            orderService.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            orderService.startPreparation(orderId, REST_ID, UserRole.RESTAURANT_OWNER);

            // "Cancellation is not allowed once preparation starts."
            try {
                orderService.cancelOrder(orderId, CUST_ID, "Please cancel food");
                fail("Test 12: Expected InvalidOrderStateException when cancelling PREPARING order");
            } catch (InvalidOrderStateException e) {
                assertTrue(e.getMessage().contains("Cancellation is not allowed once preparation starts"),
                        "Message must prohibit cancellation during preparation");
            }

            pass("Test 12: Cancellation is strictly prohibited once order preparation starts");
        } catch (Exception e) {
            fail("Test 12: Reject cancellation after preparation starts", e);
        }
    }

    private void test13_restaurantRejectionOnlyInPlacedWithReason() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            // 1. Rejection without a reason must fail
            try {
                orderService.rejectOrder(orderId, REST_ID, "   ", UserRole.RESTAURANT_OWNER);
                fail("Test 13: Expected validation exception for blank rejection reason");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("Rejection reason is mandatory"), "Must demand a reason");
            }

            // 2. Valid rejection in PLACED with reason
            Order rejectedOrder = orderService.rejectOrder(orderId, REST_ID, "Kitchen overloaded with catering orders", UserRole.RESTAURANT_OWNER);
            assertEquals(OrderStatus.REJECTED, rejectedOrder.getStatus(), "Status should be REJECTED");
            assertEquals("Kitchen overloaded with catering orders", rejectedOrder.getRejectionReason(), "Reason should match");
            assertNotNull(rejectedOrder.getRejectedAt(), "Rejection timestamp must be recorded");

            // 3. Rejection when already ACCEPTED must be rejected
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order2 = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            orderService.acceptOrder(order2.getId(), REST_ID, UserRole.RESTAURANT_OWNER);

            try {
                orderService.rejectOrder(order2.getId(), REST_ID, "Ingredient issue", UserRole.RESTAURANT_OWNER);
                fail("Test 13: Expected InvalidOrderStateException when rejecting ACCEPTED order");
            } catch (InvalidOrderStateException e) {
                assertTrue(e.getMessage().contains("only while it is in PLACED status"), "Only PLACED can be rejected");
            }

            pass("Test 13: Restaurant can reject an order only in PLACED status and must give a reason");
        } catch (Exception e) {
            fail("Test 13: Restaurant rejection only in PLACED with reason", e);
        }
    }

    private void test14_fiveMinuteAcceptanceTimeoutAutoCancellation() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            // Simulate restaurant trying to accept after 6 minutes (exceeds 5-minute timeout window)
            Instant placementTime = order.getPlacedAt();
            Instant lateAcceptTime = placementTime.plus(Duration.ofMinutes(6));

            try {
                orderService.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER, lateAcceptTime);
                fail("Test 14: Expected OrderTimeoutException when accepting after 5 minutes");
            } catch (OrderTimeoutException e) {
                assertTrue(e.getMessage().contains("automatically cancelled because it was not accepted within 5 minutes"),
                        "Message must explain timeout auto-cancellation");
            }

            // Verify order was transitioned to CANCELLED
            Order retrieved = orderService.getOrderById(orderId);
            assertEquals(OrderStatus.CANCELLED, retrieved.getStatus(), "Order must be auto-cancelled");
            assertTrue(retrieved.getCancellationReason().contains("Auto-cancelled: Restaurant did not accept within 5 minutes"),
                    "Cancellation reason must record timeout");

            pass("Test 14: Restaurant must accept within 5 minutes, otherwise order is automatically cancelled");
        } catch (Exception e) {
            fail("Test 14: Five-minute acceptance timeout auto-cancellation", e);
        }
    }

    private void test15_activeOrderCheckerPreventsRestaurantDeletion() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);
            Order order = orderService.placeOrder(CUST_ID, ADDR_ID, LocalTime.of(12, 0));
            String orderId = order.getId();

            // Attempt to delete restaurant while order is in PLACED status (active)
            try {
                restaurantService.deleteRestaurant(REST_ID, UserRole.ADMIN);
                fail("Test 15: Expected RestaurantStateException when deleting restaurant with active order");
            } catch (RestaurantStateException e) {
                assertTrue(e.getMessage().contains("has active orders"),
                        "ActiveOrderChecker must block restaurant deletion");
            }

            // Move order to DELIVERED
            orderService.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            orderService.startPreparation(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            orderService.markReadyForPickup(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            orderService.markOutForDelivery(orderId, "DP-101");
            orderService.markDelivered(orderId);

            // Now that order is DELIVERED, restaurant deletion must succeed!
            restaurantService.deleteRestaurant(REST_ID, UserRole.ADMIN);
            assertFalse(restaurantRepo.existsById(REST_ID), "Restaurant should be successfully deleted once order is delivered");

            pass("Test 15: ActiveOrderChecker interface integration prevents restaurant deletion until all orders are inactive");
        } catch (Exception e) {
            fail("Test 15: ActiveOrderChecker prevents restaurant deletion", e);
        }
    }

    private void test16_orderControllerRestEndpoints() {
        setUp();
        try {
            cartService.addItemToCart(CUST_ID, REST_ID, ITEM_1, 1);

            // 1. POST /api/orders
            PlaceOrderRequest request = new PlaceOrderRequest(CUST_ID, ADDR_ID);
            ApiResponse<OrderResponse> placeResp = orderController.placeOrder(request);
            assertEquals(201, placeResp.getStatusCode(), "Place order response status should be 201 Created");
            assertNotNull(placeResp.getData(), "Response data must contain OrderResponse");

            String orderId = placeResp.getData().getOrderId();

            // 2. GET /api/orders/{id}
            ApiResponse<OrderResponse> getResp = orderController.getOrder(orderId);
            assertEquals(200, getResp.getStatusCode(), "Get order should return 200 OK");
            assertEquals(orderId, getResp.getData().getOrderId(), "Order ID should match");

            // 3. POST /api/orders/{id}/accept
            ApiResponse<OrderResponse> acceptResp = orderController.acceptOrder(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(200, acceptResp.getStatusCode(), "Accept order should return 200 OK");
            assertEquals(OrderStatus.ACCEPTED, acceptResp.getData().getStatus(), "Status should be ACCEPTED");

            // 4. POST /api/orders/{id}/prepare
            ApiResponse<OrderResponse> prepResp = orderController.startPreparation(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(200, prepResp.getStatusCode(), "Prepare order should return 200 OK");
            assertEquals(OrderStatus.PREPARING, prepResp.getData().getStatus(), "Status should be PREPARING");

            // 5. POST /api/orders/{id}/ready
            ApiResponse<OrderResponse> readyResp = orderController.markReadyForPickup(orderId, REST_ID, UserRole.RESTAURANT_OWNER);
            assertEquals(200, readyResp.getStatusCode(), "Ready order should return 200 OK");
            assertEquals(OrderStatus.READY_FOR_PICKUP, readyResp.getData().getStatus(), "Status should be READY_FOR_PICKUP");

            pass("Test 16: OrderController REST API endpoints correctly handle HTTP status codes and responses");
        } catch (Exception e) {
            fail("Test 16: OrderController REST endpoints", e);
        }
    }

    // --- Assertion Helpers ---

    private void pass(String testName) {
        System.out.println("[PASS] " + testName);
        passed++;
    }

    private void fail(String message) {
        System.out.println("[FAIL] " + message);
        failed++;
        throw new AssertionError(message);
    }

    private void fail(String testName, Throwable t) {
        System.out.println("[FAIL] " + testName + " - " + t.getMessage());
        t.printStackTrace();
        failed++;
    }

    private void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(msg + " Expected: <" + expected + ">, but was: <" + actual + ">");
    }

    private void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg + " Expected: true, but was: false");
        }
    }

    private void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new AssertionError(msg + " Expected: false, but was: true");
        }
    }

    private void assertNotNull(Object obj, String msg) {
        if (obj == null) {
            throw new AssertionError(msg + " Expected: not null, but was: null");
        }
    }
}
