package com.food_delivery.cart.test;

import com.food_delivery.cart.controller.CartController;
import com.food_delivery.cart.dto.AddToCartRequest;
import com.food_delivery.cart.dto.CartResponse;
import com.food_delivery.cart.dto.UpdateCartItemRequest;
import com.food_delivery.cart.exception.InvalidQuantityException;
import com.food_delivery.cart.exception.MenuItemUnavailableException;
import com.food_delivery.cart.exception.RestaurantMismatchException;
import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.model.CartItem;
import com.food_delivery.cart.repository.CartRepository;
import com.food_delivery.cart.repository.InMemoryCartRepository;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.cart.service.CartServiceImpl;
import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.customer.repository.InMemoryCustomerRepository;
import com.food_delivery.customer.service.CustomerService;
import com.food_delivery.customer.service.CustomerServiceImpl;
import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.ResourceNotFoundException;
import com.food_delivery.restaurant.exception.ValidationException;
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
import java.time.LocalTime;

/**
 * Deterministic Unit and Business Rule Test Suite for Cart Management (18 Test Cases).
 */
public class CartTestSuite {

    private RestaurantRepository restaurantRepo;
    private MenuItemRepository menuRepo;
    private RestaurantService restaurantService;
    private MenuService menuService;

    private CustomerRepository customerRepo;
    private CustomerService customerService;

    private CartRepository cartRepo;
    private CartService cartService;
    private CartController cartController;

    private static final String CUST_ID = "CUST-100";
    private static final String REST_A = "101";
    private static final String REST_B = "102";
    private static final String ITEM_VEG_BIRIYANI = "1001";
    private static final String ITEM_CHICKEN_BIRIYANI = "1002";
    private static final String ITEM_PANEER_TIKKA = "1003"; // Unavailable
    private static final String ITEM_PIZZA = "2001"; // Restaurant B

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("           RUNNING FOOD DELIVERY APP - CART TEST SUITE (18 TEST CASES)          ");
        System.out.println("================================================================================");

        CartTestSuite suite = new CartTestSuite();
        int passed = 0;
        int failed = 0;

        try {
            suite.setUp(); suite.test1_addAvailableItemToEmptyCart_Success(); passed++; System.out.println("[PASS] Cart Test 1: Add an available item to an empty cart — SUCCESS");
            suite.setUp(); suite.test2_addAnotherItemFromSameRestaurant_Success(); passed++; System.out.println("[PASS] Cart Test 2: Add another item from the same restaurant — SUCCESS");
            suite.setUp(); suite.test3_addItemFromAnotherRestaurant_Failure(); passed++; System.out.println("[PASS] Cart Test 3: Add item from another restaurant — FAILURE");
            suite.setUp(); suite.test4_addUnavailableItem_Failure(); passed++; System.out.println("[PASS] Cart Test 4: Add an unavailable item — FAILURE");
            suite.setUp(); suite.test5_addItemDoesNotExist_Failure(); passed++; System.out.println("[PASS] Cart Test 5: Add an item that does not exist — FAILURE");
            suite.setUp(); suite.test6_addItemBelongingToDifferentRestaurant_Failure(); passed++; System.out.println("[PASS] Cart Test 6: Add an item belonging to a different restaurant — FAILURE");
            suite.setUp(); suite.test7_addQuantity1_Success(); passed++; System.out.println("[PASS] Cart Test 7: Add quantity 1 — SUCCESS");
            suite.setUp(); suite.test8_addQuantity10_Success(); passed++; System.out.println("[PASS] Cart Test 8: Add quantity 10 — SUCCESS");
            suite.setUp(); suite.test9_addQuantity11_Failure(); passed++; System.out.println("[PASS] Cart Test 9: Add quantity 11 — FAILURE");
            suite.setUp(); suite.test10_addSameItemMultipleTimes_QuantityIncreases(); passed++; System.out.println("[PASS] Cart Test 10: Add the same item multiple times — quantity should increase");
            suite.setUp(); suite.test11_updateQuantity_Success(); passed++; System.out.println("[PASS] Cart Test 11: Update quantity — SUCCESS");
            suite.setUp(); suite.test12_updateQuantityTo0_Failure(); passed++; System.out.println("[PASS] Cart Test 12: Update quantity to 0 — FAILURE");
            suite.setUp(); suite.test13_removeItem_Success(); passed++; System.out.println("[PASS] Cart Test 13: Remove an item — SUCCESS");
            suite.setUp(); suite.test14_clearCart_Success(); passed++; System.out.println("[PASS] Cart Test 14: Clear the cart — SUCCESS");
            suite.setUp(); suite.test15_verifyTotalCalculation_Success(); passed++; System.out.println("[PASS] Cart Test 15: Verify total calculation — SUCCESS");
            suite.setUp(); suite.test16_verifyTotalRecalculationAfterQuantityUpdate_Success(); passed++; System.out.println("[PASS] Cart Test 16: Verify total recalculation after quantity update — SUCCESS");
            suite.setUp(); suite.test17_verifyRestaurantAssociationResetsAfterRemovingAllItems_Success(); passed++; System.out.println("[PASS] Cart Test 17: Verify restaurant association resets after removing all items — SUCCESS");
            suite.setUp(); suite.test18_cartControllerEndpoints_Success(); passed++; System.out.println("[PASS] Cart Test 18: Cart controller REST API endpoints — SUCCESS");
        } catch (Throwable t) {
            failed++;
            System.err.println("\n[FAIL] Cart test execution encountered failure:");
            t.printStackTrace();
        }

        System.out.println("================================================================================");
        System.out.println("CART TESTS: " + (passed + failed) + " | PASSED: " + passed + " | FAILED: " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    public void setUp() {
        // Module 1 setup
        restaurantRepo = new InMemoryRestaurantRepository();
        menuRepo = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantServiceImpl(restaurantRepo, menuRepo);
        menuService = new MenuServiceImpl(menuRepo, restaurantRepo);

        // Populate test restaurants
        Restaurant rA = new Restaurant(REST_A, "ABC Restaurant", CuisineType.NORTH_INDIAN, "Coimbatore",
                LocalTime.of(9, 0), LocalTime.of(23, 0), new BigDecimal("100.00"));
        Restaurant rB = new Restaurant(REST_B, "XYZ Restaurant", CuisineType.ITALIAN, "Chennai",
                LocalTime.of(10, 0), LocalTime.of(22, 0), new BigDecimal("150.00"));

        restaurantService.addRestaurant(rA, UserRole.ADMIN);
        restaurantService.addRestaurant(rB, UserRole.ADMIN);

        // Populate test menu items
        menuService.addMenuItem(new MenuItem(ITEM_VEG_BIRIYANI, REST_A, "Veg Biriyani", new BigDecimal("150.00"), FoodCategory.VEG, true), UserRole.ADMIN);
        menuService.addMenuItem(new MenuItem(ITEM_CHICKEN_BIRIYANI, REST_A, "Chicken Biriyani", new BigDecimal("200.00"), FoodCategory.NON_VEG, true), UserRole.ADMIN);
        menuService.addMenuItem(new MenuItem(ITEM_PANEER_TIKKA, REST_A, "Paneer Tikka", new BigDecimal("180.00"), FoodCategory.VEG, false), UserRole.ADMIN);
        menuService.addMenuItem(new MenuItem(ITEM_PIZZA, REST_B, "Pizza", new BigDecimal("250.00"), FoodCategory.VEG, true), UserRole.ADMIN);

        // Customer setup
        customerRepo = new InMemoryCustomerRepository();
        customerService = new CustomerServiceImpl(customerRepo);

        AddressRequest addr = new AddressRequest("Villa 1", "Cross Road", "RS Puram", "Coimbatore", "Tamil Nadu", "641002", "Near Park");
        CustomerRegistrationRequest custReq = new CustomerRegistrationRequest(CUST_ID, "Test Customer", "9876543210", "test@example.com", addr);
        customerService.registerCustomer(custReq);

        // Cart setup
        cartRepo = new InMemoryCartRepository();
        cartService = new CartServiceImpl(cartRepo, customerRepo, restaurantService, menuService);
        cartController = new CartController(cartService);
    }

    // 1. Add an available item to an empty cart — SUCCESS
    public void test1_addAvailableItemToEmptyCart_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2);

        assertTrue(cart != null, "Cart should not be null");
        assertEquals(REST_A, cart.getRestaurantId(), "Cart restaurant should be set to Restaurant A");
        assertEquals(1, cart.getItems().size(), "Cart should contain 1 item");
        CartItem item = cart.getItems().get(0);
        assertEquals(ITEM_VEG_BIRIYANI, item.getMenuItemId(), "Menu item ID matches");
        assertEquals("Veg Biriyani", item.getItemName(), "Item name matches");
        assertEquals(2, item.getQuantity(), "Quantity matches");
        assertEquals(0, new BigDecimal("300.00").compareTo(item.getSubtotal()), "Subtotal is 150 * 2 = 300.00");
        assertEquals(0, new BigDecimal("300.00").compareTo(cart.getTotalAmount()), "Cart total is 300.00");
    }

    // 2. Add another item from the same restaurant — SUCCESS
    public void test2_addAnotherItemFromSameRestaurant_Success() {
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 1);
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_CHICKEN_BIRIYANI, 2);

        assertEquals(2, cart.getItems().size(), "Cart should contain 2 distinct items");
        assertEquals(REST_A, cart.getRestaurantId(), "Cart restaurant remains Restaurant A");
        // Total: (150 * 1) + (200 * 2) = 150 + 400 = 550.00
        assertEquals(0, new BigDecimal("550.00").compareTo(cart.getTotalAmount()), "Cart total is 550.00");
    }

    // 3. Add item from another restaurant — FAILURE
    public void test3_addItemFromAnotherRestaurant_Failure() {
        // Add item from Restaurant A
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 1);

        // Attempt to add Pizza from Restaurant B
        assertThrows(RestaurantMismatchException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_B, ITEM_PIZZA, 1),
                "Should reject item from a different restaurant");

        // Verify cart was NOT cleared and still contains Restaurant A item
        Cart cart = cartService.getCartByCustomerId(CUST_ID);
        assertEquals(REST_A, cart.getRestaurantId(), "Cart should still be associated with Restaurant A");
        assertEquals(1, cart.getItems().size(), "Cart should still retain original item");
        assertEquals(ITEM_VEG_BIRIYANI, cart.getItems().get(0).getMenuItemId(), "Original item preserved");
    }

    // 4. Add an unavailable item — FAILURE
    public void test4_addUnavailableItem_Failure() {
        assertThrows(MenuItemUnavailableException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A, ITEM_PANEER_TIKKA, 1),
                "Should reject unavailable menu item");
    }

    // 5. Add an item that does not exist — FAILURE
    public void test5_addItemDoesNotExist_Failure() {
        assertThrows(ResourceNotFoundException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A, "9999", 1),
                "Should reject non-existent menu item");
    }

    // 6. Add an item belonging to a different restaurant — FAILURE
    public void test6_addItemBelongingToDifferentRestaurant_Failure() {
        // ITEM_PIZZA belongs to REST_B, but requesting under REST_A
        assertThrows(ValidationException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A, ITEM_PIZZA, 1),
                "Should reject when menuItem does not belong to specified restaurant");
    }

    // 7. Add quantity 1 — SUCCESS
    public void test7_addQuantity1_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 1);
        assertEquals(1, cart.getItems().get(0).getQuantity(), "Quantity should be 1");
        assertEquals(0, new BigDecimal("150.00").compareTo(cart.getTotalAmount()), "Total amount is 150.00");
    }

    // 8. Add quantity 10 — SUCCESS
    public void test8_addQuantity10_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 10);
        assertEquals(10, cart.getItems().get(0).getQuantity(), "Quantity should be 10");
        assertEquals(0, new BigDecimal("1500.00").compareTo(cart.getTotalAmount()), "Total amount is 1500.00");
    }

    // 9. Add quantity 11 — FAILURE
    public void test9_addQuantity11_Failure() {
        assertThrows(InvalidQuantityException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 11),
                "Should reject quantity 11 (exceeds max 10)");
    }

    // 10. Add the same item multiple times — quantity should increase
    public void test10_addSameItemMultipleTimes_QuantityIncreases() {
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 3);
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 4);

        assertEquals(1, cart.getItems().size(), "Cart should still have only 1 item entry");
        assertEquals(7, cart.getItems().get(0).getQuantity(), "Quantity should be 3 + 4 = 7");
        assertEquals(0, new BigDecimal("1050.00").compareTo(cart.getTotalAmount()), "Total amount is 150 * 7 = 1050.00");

        // Attempting to add 4 more would exceed 10 (7 + 4 = 11) -> should fail
        assertThrows(InvalidQuantityException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 4),
                "Adding more units exceeding cumulative 10 must fail");
    }

    // 11. Update quantity — SUCCESS
    public void test11_updateQuantity_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2);
        String cartItemId = cart.getItems().get(0).getId();

        Cart updated = cartService.updateCartItemQuantity(CUST_ID, cartItemId, 5);
        assertEquals(5, updated.getItems().get(0).getQuantity(), "Quantity updated to 5");
        assertEquals(0, new BigDecimal("750.00").compareTo(updated.getTotalAmount()), "Total recalculated: 150 * 5 = 750.00");
    }

    // 12. Update quantity to 0 — FAILURE
    public void test12_updateQuantityTo0_Failure() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2);
        String cartItemId = cart.getItems().get(0).getId();

        assertThrows(InvalidQuantityException.class, () ->
                cartService.updateCartItemQuantity(CUST_ID, cartItemId, 0),
                "Should reject updating quantity to 0");

        assertThrows(InvalidQuantityException.class, () ->
                cartService.updateCartItemQuantity(CUST_ID, cartItemId, -1),
                "Should reject updating quantity to negative");
    }

    // 13. Remove an item — SUCCESS
    public void test13_removeItem_Success() {
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 1);
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_CHICKEN_BIRIYANI, 1);
        assertEquals(2, cart.getItems().size(), "Cart has 2 items");

        String vegItemId = cart.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(ITEM_VEG_BIRIYANI))
                .findFirst().get().getId();

        Cart updated = cartService.removeCartItem(CUST_ID, vegItemId);
        assertEquals(1, updated.getItems().size(), "Cart now has 1 item");
        assertEquals(ITEM_CHICKEN_BIRIYANI, updated.getItems().get(0).getMenuItemId(), "Remaining item is Chicken Biriyani");
        assertEquals(0, new BigDecimal("200.00").compareTo(updated.getTotalAmount()), "Total is 200.00");
    }

    // 14. Clear the cart — SUCCESS
    public void test14_clearCart_Success() {
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2);
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_CHICKEN_BIRIYANI, 1);

        Cart cleared = cartService.clearCart(CUST_ID);
        assertTrue(cleared.isEmpty(), "Cart should be empty");
        assertEquals(null, cleared.getRestaurantId(), "Restaurant association should be reset to null");
        assertEquals(0, BigDecimal.ZERO.compareTo(cleared.getTotalAmount()), "Total amount should be zero");
    }

    // 15. Verify total calculation — SUCCESS
    public void test15_verifyTotalCalculation_Success() {
        // 3 Veg Biriyani (150 * 3 = 450) + 2 Chicken Biriyani (200 * 2 = 400) = 850.00
        cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 3);
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_CHICKEN_BIRIYANI, 2);

        assertEquals(0, new BigDecimal("850.00").compareTo(cart.getTotalAmount()), "Total should be 850.00");
    }

    // 16. Verify total recalculation after quantity update — SUCCESS
    public void test16_verifyTotalRecalculationAfterQuantityUpdate_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2); // 300
        String cartItemId = cart.getItems().get(0).getId();

        // Update to 8 units (150 * 8 = 1200.00)
        Cart updated = cartService.updateCartItemQuantity(CUST_ID, cartItemId, 8);
        assertEquals(0, new BigDecimal("1200.00").compareTo(updated.getTotalAmount()), "Total recalculated after update to 8");

        // Update to 1 unit (150 * 1 = 150.00)
        updated = cartService.updateCartItemQuantity(CUST_ID, cartItemId, 1);
        assertEquals(0, new BigDecimal("150.00").compareTo(updated.getTotalAmount()), "Total recalculated after update to 1");
    }

    // 17. Verify restaurant association resets after removing all items — SUCCESS
    public void test17_verifyRestaurantAssociationResetsAfterRemovingAllItems_Success() {
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 1);
        assertEquals(REST_A, cart.getRestaurantId(), "Associated with Restaurant A");

        String cartItemId = cart.getItems().get(0).getId();
        Cart afterRemove = cartService.removeCartItem(CUST_ID, cartItemId);

        assertTrue(afterRemove.isEmpty(), "Cart is empty");
        assertEquals(null, afterRemove.getRestaurantId(), "Restaurant association must reset to null after removing all items");
        assertEquals(0, BigDecimal.ZERO.compareTo(afterRemove.getTotalAmount()), "Total is 0");

        // Now customer can add an item from Restaurant B without conflict!
        Cart cartB = cartService.addItemToCart(CUST_ID, REST_B, ITEM_PIZZA, 1);
        assertEquals(REST_B, cartB.getRestaurantId(), "Now associated with Restaurant B");
        assertEquals(1, cartB.getItems().size(), "Contains 1 item from Restaurant B");
    }

    // 18. Cart controller REST API endpoints — SUCCESS
    public void test18_cartControllerEndpoints_Success() {
        AddToCartRequest addReq = new AddToCartRequest(CUST_ID, REST_A, ITEM_VEG_BIRIYANI, 2);
        ApiResponse<CartResponse> addResp = cartController.addItem(CUST_ID, addReq);
        assertEquals(200, addResp.getStatusCode(), "Controller add item returns 200 OK");
        assertEquals(1, addResp.getData().getItems().size(), "1 item in cart response");
        assertEquals(0, new BigDecimal("300.00").compareTo(addResp.getData().getTotalAmount()), "Subtotal is 300.00");

        ApiResponse<CartResponse> getResp = cartController.getCart(CUST_ID);
        assertEquals(200, getResp.getStatusCode(), "Controller get cart returns 200 OK");

        String cartItemId = addResp.getData().getItems().get(0).getId();
        ApiResponse<CartResponse> updateResp = cartController.updateItemQuantity(CUST_ID, cartItemId, new UpdateCartItemRequest(3));
        assertEquals(200, updateResp.getStatusCode(), "Controller update quantity returns 200 OK");
        assertEquals(3, updateResp.getData().getItems().get(0).getQuantity(), "Updated quantity is 3");

        ApiResponse<CartResponse> clearResp = cartController.clearCart(CUST_ID);
        assertEquals(200, clearResp.getStatusCode(), "Controller clear cart returns 200 OK");
        assertEquals(0, clearResp.getData().getItems().size(), "Cart cleared");
    }

    // --- Assertion Utilities ---
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Assertion Failed: " + message + " [Expected: " + expected + ", Actual: " + actual + "]");
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedException, Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but none thrown. " + message);
        } catch (Throwable t) {
            if (!expectedException.isInstance(t)) {
                throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage() + ". " + message, t);
            }
        }
    }
}
