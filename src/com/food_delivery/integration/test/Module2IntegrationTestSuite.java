package com.food_delivery.integration.test;

import com.food_delivery.cart.exception.MenuItemUnavailableException;
import com.food_delivery.cart.exception.RestaurantMismatchException;
import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.repository.InMemoryCartRepository;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.cart.service.CartServiceImpl;
import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.model.Customer;
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
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.MenuServiceImpl;
import com.food_delivery.restaurant.service.RestaurantService;
import com.food_delivery.restaurant.service.RestaurantServiceImpl;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * End-to-End Integration Test Suite validating the integration between Module 1 (Restaurant & Menu)
 * and Module 2 (Customer & Cart), using the required specification test dataset.
 */
public class Module2IntegrationTestSuite {

    private InMemoryRestaurantRepository restaurantRepo;
    private InMemoryMenuItemRepository menuRepo;
    private RestaurantService restaurantService;
    private MenuService menuService;

    private InMemoryCustomerRepository customerRepo;
    private CustomerService customerService;

    private InMemoryCartRepository cartRepo;
    private CartService cartService;

    // Test Data Specification
    private static final String REST_A_ID = "101";
    private static final String REST_B_ID = "102";

    private static final String ITEM_VEG_BIRIYANI_ID = "1001";
    private static final String ITEM_CHICKEN_BIRIYANI_ID = "1002";
    private static final String ITEM_PANEER_TIKKA_ID = "1003";
    private static final String ITEM_PIZZA_ID = "2001";

    private static final String CUST_ID = "CUST-SPEC";

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   RUNNING FOOD DELIVERY APP - MODULE 1 & MODULE 2 INTEGRATION TEST SUITE       ");
        System.out.println("================================================================================");

        Module2IntegrationTestSuite suite = new Module2IntegrationTestSuite();
        int passed = 0;
        int failed = 0;

        try {
            suite.setUp(); suite.test1_verifyRestaurantDataRetrievedCorrectly(); passed++; System.out.println("[PASS] Integration Test 1: Restaurant data is retrieved correctly");
            suite.setUp(); suite.test2_verifyMenuItemDataRetrievedCorrectly(); passed++; System.out.println("[PASS] Integration Test 2: Menu item data is retrieved correctly");
            suite.setUp(); suite.test3_verifyMenuAvailabilityCheckedCorrectly(); passed++; System.out.println("[PASS] Integration Test 3: Menu availability is checked correctly");
            suite.setUp(); suite.test4_verifyCurrentMenuItemPricesRetrievedCorrectly(); passed++; System.out.println("[PASS] Integration Test 4: Current menu item prices are retrieved correctly");
            suite.setUp(); suite.test5_verifyInvalidRestaurantMenuItemRelationshipRejected(); passed++; System.out.println("[PASS] Integration Test 5: Invalid restaurant/menu item relationships are rejected");
            suite.setUp(); suite.test6_dynamicPriceUpdateInModule1ReflectedInCart(); passed++; System.out.println("[PASS] Integration Test 6: Dynamic price updates in Module 1 are reflected in new cart additions");
            suite.setUp(); suite.test7_dynamicAvailabilityToggleInModule1ReflectedInCart(); passed++; System.out.println("[PASS] Integration Test 7: Dynamic availability toggle in Module 1 is reflected in cart operations");
            suite.setUp(); suite.test8_module3CleanCartClearingWorkflow(); passed++; System.out.println("[PASS] Integration Test 8: Clean cart-clearing service interface for Module 3 order placement");
        } catch (Throwable t) {
            failed++;
            System.err.println("\n[FAIL] Integration test execution encountered failure:");
            t.printStackTrace();
        }

        System.out.println("================================================================================");
        System.out.println("INTEGRATION TESTS: " + (passed + failed) + " | PASSED: " + passed + " | FAILED: " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    public void setUp() {
        // Module 1 Layer
        restaurantRepo = new InMemoryRestaurantRepository();
        menuRepo = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantServiceImpl(restaurantRepo, menuRepo);
        menuService = new MenuServiceImpl(menuRepo, restaurantRepo);

        // Register Restaurant A
        Restaurant restA = new Restaurant(
                REST_A_ID, "ABC Restaurant", CuisineType.NORTH_INDIAN, "Coimbatore",
                LocalTime.of(8, 0), LocalTime.of(23, 0), new BigDecimal("100.00")
        );
        restaurantService.addRestaurant(restA, UserRole.ADMIN);
        restaurantService.activateRestaurant(REST_A_ID, UserRole.ADMIN);
        restaurantService.markOpen(REST_A_ID, UserRole.ADMIN);

        // Register Restaurant B
        Restaurant restB = new Restaurant(
                REST_B_ID, "XYZ Restaurant", CuisineType.ITALIAN, "Chennai",
                LocalTime.of(9, 0), LocalTime.of(22, 0), new BigDecimal("150.00")
        );
        restaurantService.addRestaurant(restB, UserRole.ADMIN);
        restaurantService.activateRestaurant(REST_B_ID, UserRole.ADMIN);
        restaurantService.markOpen(REST_B_ID, UserRole.ADMIN);

        // Register Menu Items for Restaurant A
        menuService.addMenuItem(new MenuItem(ITEM_VEG_BIRIYANI_ID, REST_A_ID, "Veg Biriyani", new BigDecimal("150.00"), FoodCategory.VEG, true), UserRole.ADMIN);
        menuService.addMenuItem(new MenuItem(ITEM_CHICKEN_BIRIYANI_ID, REST_A_ID, "Chicken Biriyani", new BigDecimal("200.00"), FoodCategory.NON_VEG, true), UserRole.ADMIN);
        menuService.addMenuItem(new MenuItem(ITEM_PANEER_TIKKA_ID, REST_A_ID, "Paneer Tikka", new BigDecimal("180.00"), FoodCategory.VEG, false), UserRole.ADMIN);

        // Register Menu Items for Restaurant B
        menuService.addMenuItem(new MenuItem(ITEM_PIZZA_ID, REST_B_ID, "Pizza", new BigDecimal("250.00"), FoodCategory.VEG, true), UserRole.ADMIN);

        // Module 2 Customer Layer
        customerRepo = new InMemoryCustomerRepository();
        customerService = new CustomerServiceImpl(customerRepo);

        AddressRequest address = new AddressRequest(
                "No. 42, Sunrise Enclave", "Avinashi Road", "Peelamedu", "Coimbatore", "Tamil Nadu", "641004", "Near Tech Park"
        );
        CustomerRegistrationRequest custReq = new CustomerRegistrationRequest(
                CUST_ID, "Test Customer", "9876543210", "test@example.com", address
        );
        customerService.registerCustomer(custReq);

        // Module 2 Cart Layer
        cartRepo = new InMemoryCartRepository();
        cartService = new CartServiceImpl(cartRepo, customerRepo, restaurantService, menuService);
    }

    // 1. Restaurant data is retrieved correctly
    public void test1_verifyRestaurantDataRetrievedCorrectly() {
        Restaurant rA = restaurantService.getRestaurantById(REST_A_ID);
        assertEquals("ABC Restaurant", rA.getName(), "Restaurant A name matches");
        assertEquals("Coimbatore", rA.getCity(), "Restaurant A city matches");

        Restaurant rB = restaurantService.getRestaurantById(REST_B_ID);
        assertEquals("XYZ Restaurant", rB.getName(), "Restaurant B name matches");
        assertEquals("Chennai", rB.getCity(), "Restaurant B city matches");
    }

    // 2. Menu item data is retrieved correctly
    public void test2_verifyMenuItemDataRetrievedCorrectly() {
        MenuItem m1001 = menuService.getMenuItemById(ITEM_VEG_BIRIYANI_ID);
        assertEquals("Veg Biriyani", m1001.getName(), "Item name matches");
        assertEquals(REST_A_ID, m1001.getRestaurantId(), "Restaurant ID matches");
        assertEquals(0, new BigDecimal("150.00").compareTo(m1001.getPrice()), "Item price matches");

        MenuItem m2001 = menuService.getMenuItemById(ITEM_PIZZA_ID);
        assertEquals("Pizza", m2001.getName(), "Item name matches");
        assertEquals(REST_B_ID, m2001.getRestaurantId(), "Restaurant ID matches");
        assertEquals(0, new BigDecimal("250.00").compareTo(m2001.getPrice()), "Item price matches");
    }

    // 3. Menu availability is checked correctly
    public void test3_verifyMenuAvailabilityCheckedCorrectly() {
        assertTrue(menuService.isAvailable(ITEM_VEG_BIRIYANI_ID), "Veg Biriyani should be available");
        assertTrue(menuService.isAvailable(ITEM_CHICKEN_BIRIYANI_ID), "Chicken Biriyani should be available");
        assertFalse(menuService.isAvailable(ITEM_PANEER_TIKKA_ID), "Paneer Tikka should be unavailable");

        // Attempting to add unavailable Paneer Tikka into Cart must fail
        assertThrows(MenuItemUnavailableException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_PANEER_TIKKA_ID, 1),
                "Should reject adding unavailable menu item to cart");
    }

    // 4. Current menu item prices are retrieved correctly
    public void test4_verifyCurrentMenuItemPricesRetrievedCorrectly() {
        assertEquals(0, new BigDecimal("150.00").compareTo(menuService.getItemPrice(ITEM_VEG_BIRIYANI_ID)), "Price for 1001 is 150.00");
        assertEquals(0, new BigDecimal("200.00").compareTo(menuService.getItemPrice(ITEM_CHICKEN_BIRIYANI_ID)), "Price for 1002 is 200.00");
        assertEquals(0, new BigDecimal("250.00").compareTo(menuService.getItemPrice(ITEM_PIZZA_ID)), "Price for 2001 is 250.00");

        // Adding to cart uses the live retrieved price
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_VEG_BIRIYANI_ID, 2);
        assertEquals(0, new BigDecimal("300.00").compareTo(cart.getTotalAmount()), "Cart total computed from live price: 150 * 2 = 300.00");
    }

    // 5. Invalid restaurant/menu item relationships are rejected
    public void test5_verifyInvalidRestaurantMenuItemRelationshipRejected() {
        // Pizza (2001) belongs to Restaurant B (102). Trying to add it under Restaurant A (101) must fail.
        assertThrows(ValidationException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_PIZZA_ID, 1),
                "Item belonging to Restaurant B cannot be added under Restaurant A");

        // Veg Biriyani (1001) belongs to Restaurant A (101). Trying to add it under Restaurant B (102) must fail.
        assertThrows(ValidationException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_B_ID, ITEM_VEG_BIRIYANI_ID, 1),
                "Item belonging to Restaurant A cannot be added under Restaurant B");

        // Non-existent restaurant ID
        assertThrows(ResourceNotFoundException.class, () ->
                cartService.addItemToCart(CUST_ID, "999", ITEM_VEG_BIRIYANI_ID, 1),
                "Non-existent restaurant must be rejected");

        // Non-existent menu item ID
        assertThrows(ResourceNotFoundException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A_ID, "8888", 1),
                "Non-existent menu item must be rejected");
    }

    // 6. Dynamic price updates in Module 1 are reflected in new cart additions
    public void test6_dynamicPriceUpdateInModule1ReflectedInCart() {
        // Update price of Chicken Biriyani from 200 to 220 in Module 1
        menuService.updateItemPrice(ITEM_CHICKEN_BIRIYANI_ID, new BigDecimal("220.00"), UserRole.ADMIN);

        // Add to cart in Module 2
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_CHICKEN_BIRIYANI_ID, 2);
        assertEquals(0, new BigDecimal("220.00").compareTo(cart.getItems().get(0).getItemPrice()), "Cart item uses updated price 220.00");
        assertEquals(0, new BigDecimal("440.00").compareTo(cart.getTotalAmount()), "Cart total uses updated price: 220 * 2 = 440.00");
    }

    // 7. Dynamic availability toggle in Module 1 is reflected in cart operations
    public void test7_dynamicAvailabilityToggleInModule1ReflectedInCart() {
        // Paneer Tikka (1003) is initially unavailable
        assertThrows(MenuItemUnavailableException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_PANEER_TIKKA_ID, 1),
                "Cannot add initially unavailable item");

        // Restaurant manager marks it Available in Module 1
        menuService.markItemAvailable(ITEM_PANEER_TIKKA_ID, UserRole.RESTAURANT_OWNER);

        // Now Cart can add it successfully!
        Cart cart = cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_PANEER_TIKKA_ID, 2);
        assertEquals(1, cart.getItems().size(), "Paneer Tikka successfully added after marking available");
        assertEquals(0, new BigDecimal("360.00").compareTo(cart.getTotalAmount()), "Total: 180 * 2 = 360.00");

        // If marked unavailable again in Module 1, further additions are blocked
        menuService.markItemUnavailable(ITEM_PANEER_TIKKA_ID, UserRole.RESTAURANT_OWNER);
        assertThrows(MenuItemUnavailableException.class, () ->
                cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_PANEER_TIKKA_ID, 1),
                "Further additions blocked when marked unavailable again");
    }

    // 8. Clean cart-clearing service interface for Module 3 order placement
    public void test8_module3CleanCartClearingWorkflow() {
        cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_VEG_BIRIYANI_ID, 2);
        cartService.addItemToCart(CUST_ID, REST_A_ID, ITEM_CHICKEN_BIRIYANI_ID, 1);

        Cart activeCart = cartService.getCartByCustomerId(CUST_ID);
        assertEquals(2, activeCart.getItems().size(), "Cart has items before order placement");

        // Module 3 completes order checkout and calls cartService.clearCart(customerId)
        Cart cleared = cartService.clearCart(CUST_ID);
        assertTrue(cleared.isEmpty(), "Cart is cleared after order completion");
        assertEquals(null, cleared.getRestaurantId(), "Restaurant association is reset");
        assertEquals(0, BigDecimal.ZERO.compareTo(cleared.getTotalAmount()), "Total amount is 0");

        // Customer can now immediately start an entirely new cart from Restaurant B
        Cart newCart = cartService.addItemToCart(CUST_ID, REST_B_ID, ITEM_PIZZA_ID, 1);
        assertEquals(REST_B_ID, newCart.getRestaurantId(), "New order can target Restaurant B");
        assertEquals(1, newCart.getItems().size(), "Contains Restaurant B item");
    }

    // --- Assertion Utilities ---
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Assertion Failed (expected false, got true): " + message);
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
