package com.food_delivery.restaurant.test;

import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.DuplicateResourceException;
import com.food_delivery.restaurant.exception.ResourceNotFoundException;
import com.food_delivery.restaurant.exception.RestaurantStateException;
import com.food_delivery.restaurant.exception.UnauthorizedException;
import com.food_delivery.restaurant.exception.ValidationException;
import com.food_delivery.restaurant.integration.ActiveOrderChecker;
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
import java.util.List;

/**
 * Deterministic test suite verifying all 15 required business cases for Module 1: Restaurant and Menu Management.
 */
public class RestaurantTestSuite {

    private RestaurantRepository restaurantRepo;
    private MenuItemRepository menuRepo;
    private RestaurantService restaurantService;
    private MenuService menuService;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("         RUNNING FOOD DELIVERY APP - MODULE 1 TEST SUITE (15 TEST CASES)       ");
        System.out.println("================================================================================");

        RestaurantTestSuite suite = new RestaurantTestSuite();
        int passed = 0;
        int failed = 0;

        try {
            suite.setUp(); suite.test1_successfullyCreateValidRestaurant(); passed++; System.out.println("[PASS] Test 1: Successfully create a valid restaurant");
            suite.setUp(); suite.test2_rejectInvalidRestaurant(); passed++; System.out.println("[PASS] Test 2: Reject invalid restaurants (name, city, cuisine, times, min order)");
            suite.setUp(); suite.test3_rejectUnauthorizedRestaurantCreation(); passed++; System.out.println("[PASS] Test 3: Reject unauthorized restaurant creation request (CUSTOMER role)");
            suite.setUp(); suite.test4_preventProtectedFieldChangesAfterActivation(); passed++; System.out.println("[PASS] Test 4: Prevent changes to name, cuisine, and city after activation");
            suite.setUp(); suite.test5_allowPermittedTimingAndMinimumOrderValueUpdates(); passed++; System.out.println("[PASS] Test 5: Allow permitted timing and minimum-order-value updates after activation");
            suite.setUp(); suite.test6_correctlyDetermineAcceptingOrders(); passed++; System.out.println("[PASS] Test 6: Correctly determine whether restaurant is accepting orders (active, open, hours)");
            suite.setUp(); suite.test7_handleOpeningHoursCrossingMidnight(); passed++; System.out.println("[PASS] Test 7: Correctly handle opening hours crossing midnight (e.g., 20:00 to 02:00)");
            suite.setUp(); suite.test8_successfullyAddValidMenuItem(); passed++; System.out.println("[PASS] Test 8: Successfully add a valid menu item");
            suite.setUp(); suite.test9_rejectMenuItemWithZeroOrNegativePrice(); passed++; System.out.println("[PASS] Test 9: Reject a menu item with a zero or negative price");
            suite.setUp(); suite.test10_rejectUnauthorizedMenuChanges(); passed++; System.out.println("[PASS] Test 10: Reject unauthorized menu changes (CUSTOMER attempting modifications)");
            suite.setUp(); suite.test11_preventUnavailableItemsFromPassingAvailabilityValidation(); passed++; System.out.println("[PASS] Test 11: Prevent unavailable items from passing cart availability validation");
            suite.setUp(); suite.test12_preventRestaurantDeletionWhenActiveOrdersExist(); passed++; System.out.println("[PASS] Test 12: Prevent restaurant deletion when active orders exist or status unverified");
            suite.setUp(); suite.test13_allowDeletionWhenNoActiveOrdersExist(); passed++; System.out.println("[PASS] Test 13: Allow deletion when no active orders exist and clean up menu items");
            suite.setUp(); suite.test14_rejectDuplicateIds(); passed++; System.out.println("[PASS] Test 14: Reject duplicate restaurant IDs and duplicate menu item IDs");
            suite.setUp(); suite.test15_verifyRetrievalFromRepository(); passed++; System.out.println("[PASS] Test 15: Verify menu items and restaurants are correctly retrieved from repository");
        } catch (Throwable t) {
            failed++;
            System.err.println("\n[FAIL] Test execution encountered failure:");
            t.printStackTrace();
        }

        System.out.println("================================================================================");
        System.out.println("TOTAL TESTS: " + (passed + failed) + " | PASSED: " + passed + " | FAILED: " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private void setUp() {
        restaurantRepo = new InMemoryRestaurantRepository();
        menuRepo = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantServiceImpl(restaurantRepo, menuRepo);
        menuService = new MenuServiceImpl(menuRepo, restaurantRepo);
    }

    // --- TEST 1 ---
    public void test1_successfullyCreateValidRestaurant() {
        Restaurant r = new Restaurant(
                "REST-1",
                "Spice Villa",
                CuisineType.NORTH_INDIAN,
                "Delhi",
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                new BigDecimal("150.00")
        );
        Restaurant saved = restaurantService.addRestaurant(r, UserRole.ADMIN);
        assertTrue(saved != null, "Saved restaurant must not be null");
        assertEquals("REST-1", saved.getId(), "Restaurant ID matches");
        assertEquals("Spice Villa", saved.getName(), "Name matches");
        assertEquals(CuisineType.NORTH_INDIAN, saved.getCuisineType(), "Cuisine matches");
        assertEquals("Delhi", saved.getCity(), "City matches");
        assertFalse(saved.isActive(), "New restaurant should initially be inactive");
        assertFalse(saved.isOpen(), "New restaurant should initially be closed");
    }

    // --- TEST 2 ---
    public void test2_rejectInvalidRestaurant() {
        // Blank name
        assertThrows(ValidationException.class, () -> new Restaurant(
                "R-INV-1", "   ", CuisineType.CHINESE, "Mumbai",
                LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("100")
        ), "Should reject blank restaurant name");

        // Blank city
        assertThrows(ValidationException.class, () -> new Restaurant(
                "R-INV-2", "Dragon Express", CuisineType.CHINESE, "",
                LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("100")
        ), "Should reject blank city");

        // Null cuisine
        assertThrows(ValidationException.class, () -> new Restaurant(
                "R-INV-3", "Dragon Express", null, "Mumbai",
                LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("100")
        ), "Should reject null cuisine");

        // Null opening time
        assertThrows(ValidationException.class, () -> new Restaurant(
                "R-INV-4", "Dragon Express", CuisineType.CHINESE, "Mumbai",
                null, LocalTime.of(21, 0), new BigDecimal("100")
        ), "Should reject null opening time");

        // Negative minimum order value
        assertThrows(ValidationException.class, () -> new Restaurant(
                "R-INV-5", "Dragon Express", CuisineType.CHINESE, "Mumbai",
                LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("-10.00")
        ), "Should reject negative minimum order value");
    }

    // --- TEST 3 ---
    public void test3_rejectUnauthorizedRestaurantCreation() {
        Restaurant r = new Restaurant(
                "REST-CUST", "Tasty Bites", CuisineType.FAST_FOOD, "Bangalore",
                LocalTime.of(11, 0), LocalTime.of(23, 0), new BigDecimal("50.00")
        );
        // Customer role should be rejected
        assertThrows(UnauthorizedException.class, () -> restaurantService.addRestaurant(r, UserRole.CUSTOMER),
                "Customer role must not be allowed to create restaurant");

        // Null role should be rejected
        assertThrows(UnauthorizedException.class, () -> restaurantService.addRestaurant(r, null),
                "Null role must not be allowed to create restaurant");
    }

    // --- TEST 4 ---
    public void test4_preventProtectedFieldChangesAfterActivation() {
        Restaurant r = new Restaurant(
                "REST-ACT", "Punjab Grill", CuisineType.NORTH_INDIAN, "Chandigarh",
                LocalTime.of(12, 0), LocalTime.of(23, 0), new BigDecimal("200.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);
        restaurantService.activateRestaurant("REST-ACT", UserRole.ADMIN);

        // Attempting to update details via service after activation
        assertThrows(RestaurantStateException.class, () ->
                restaurantService.updateRestaurantDetails("REST-ACT", "Punjab Bistro", CuisineType.NORTH_INDIAN, "Chandigarh", UserRole.ADMIN),
                "Should forbid updating restaurant name after activation");

        // Direct model manipulation should also throw RestaurantStateException
        Restaurant fetched = restaurantService.getRestaurantById("REST-ACT");
        assertThrows(RestaurantStateException.class, () -> fetched.setName("Punjab Bistro"),
                "Direct setName must throw exception when active");
        assertThrows(RestaurantStateException.class, () -> fetched.setCuisineType(CuisineType.ITALIAN),
                "Direct setCuisineType must throw exception when active");
        assertThrows(RestaurantStateException.class, () -> fetched.setCity("Amritsar"),
                "Direct setCity must throw exception when active");
    }

    // --- TEST 5 ---
    public void test5_allowPermittedTimingAndMinimumOrderValueUpdates() {
        Restaurant r = new Restaurant(
                "REST-EDIT", "Pasta Bar", CuisineType.ITALIAN, "Pune",
                LocalTime.of(10, 0), LocalTime.of(22, 0), new BigDecimal("100.00")
        );
        restaurantService.addRestaurant(r, UserRole.RESTAURANT_OWNER);
        restaurantService.activateRestaurant("REST-EDIT", UserRole.RESTAURANT_OWNER);

        // Update opening and closing times
        LocalTime newOpen = LocalTime.of(11, 30);
        LocalTime newClose = LocalTime.of(23, 30);
        Restaurant updatedHours = restaurantService.updateOperatingHours("REST-EDIT", newOpen, newClose, UserRole.RESTAURANT_OWNER);
        assertEquals(newOpen, updatedHours.getOpeningTime(), "Opening time updated successfully");
        assertEquals(newClose, updatedHours.getClosingTime(), "Closing time updated successfully");

        // Update minimum order value
        BigDecimal newMin = new BigDecimal("250.00");
        Restaurant updatedMin = restaurantService.updateMinimumOrderValue("REST-EDIT", newMin, UserRole.RESTAURANT_OWNER);
        assertEquals(0, newMin.compareTo(updatedMin.getMinimumOrderValue()), "Minimum order value updated successfully");
    }

    // --- TEST 6 ---
    public void test6_correctlyDetermineAcceptingOrders() {
        Restaurant r = new Restaurant(
                "REST-HOURS", "Dosa Corner", CuisineType.SOUTH_INDIAN, "Chennai",
                LocalTime.of(8, 0), LocalTime.of(20, 0), new BigDecimal("50.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        // Case 1: Inactive and Closed
        assertFalse(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(12, 0)),
                "Inactive restaurant must not accept orders");

        // Case 2: Activated but Closed
        restaurantService.activateRestaurant("REST-HOURS", UserRole.ADMIN);
        assertFalse(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(12, 0)),
                "Activated but closed restaurant must not accept orders");

        // Case 3: Activated and Marked Open
        restaurantService.markOpen("REST-HOURS", UserRole.ADMIN);

        // Before opening time
        assertFalse(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(7, 59)),
                "Before opening hours must be rejected");

        // Exactly at opening time
        assertTrue(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(8, 0)),
                "Opening time boundary must be accepted");

        // During operating hours
        assertTrue(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(13, 0)),
                "Midday time must be accepted");

        // Exactly at closing time
        assertTrue(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(20, 0)),
                "Closing time boundary must be accepted");

        // After closing time
        assertFalse(restaurantService.isAcceptingOrders("REST-HOURS", LocalTime.of(20, 1)),
                "After closing hours must be rejected");
    }

    // --- TEST 7 ---
    public void test7_handleOpeningHoursCrossingMidnight() {
        // Restaurant open from 20:00 (8 PM) to 02:00 (2 AM)
        Restaurant r = new Restaurant(
                "REST-NIGHT", "Midnight Diner", CuisineType.FAST_FOOD, "Kolkata",
                LocalTime.of(20, 0), LocalTime.of(2, 0), new BigDecimal("100.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);
        restaurantService.activateRestaurant("REST-NIGHT", UserRole.ADMIN);
        restaurantService.markOpen("REST-NIGHT", UserRole.ADMIN);

        // Test times before opening (daytime)
        assertFalse(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(15, 0)), "15:00 should be closed");
        assertFalse(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(19, 59)), "19:59 should be closed");

        // Test night times
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(20, 0)), "20:00 (opening) should be open");
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(22, 30)), "22:30 should be open");
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(23, 59)), "23:59 should be open");

        // Test early morning after midnight
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(0, 0)), "00:00 (midnight) should be open");
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(1, 30)), "01:30 should be open");
        assertTrue(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(2, 0)), "02:00 (closing) should be open");

        // Test after closing
        assertFalse(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(2, 1)), "02:01 should be closed");
        assertFalse(restaurantService.isAcceptingOrders("REST-NIGHT", LocalTime.of(10, 0)), "10:00 should be closed");
    }

    // --- TEST 8 ---
    public void test8_successfullyAddValidMenuItem() {
        Restaurant r = new Restaurant(
                "REST-MENU", "Wok Toss", CuisineType.CHINESE, "Hyderabad",
                LocalTime.of(11, 0), LocalTime.of(23, 0), new BigDecimal("120.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        MenuItem item = new MenuItem(
                "ITEM-1",
                "REST-MENU",
                "Hakka Noodles",
                new BigDecimal("180.00"),
                FoodCategory.VEG,
                true
        );
        MenuItem saved = menuService.addMenuItem(item, UserRole.RESTAURANT_OWNER);
        assertTrue(saved != null, "Saved menu item must not be null");
        assertEquals("ITEM-1", saved.getId(), "Menu item ID matches");
        assertEquals("Hakka Noodles", saved.getName(), "Item name matches");
        assertEquals(0, new BigDecimal("180.00").compareTo(saved.getPrice()), "Price matches");
        assertEquals(FoodCategory.VEG, saved.getCategory(), "Category matches");
        assertTrue(saved.isAvailable(), "Availability should be true");
    }

    // --- TEST 9 ---
    public void test9_rejectMenuItemWithZeroOrNegativePrice() {
        Restaurant r = new Restaurant(
                "REST-PRICE", "Curry Leaf", CuisineType.SOUTH_INDIAN, "Mysore",
                LocalTime.of(7, 0), LocalTime.of(22, 0), new BigDecimal("50.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        // Zero price
        assertThrows(ValidationException.class, () -> new MenuItem(
                "ITEM-ZERO", "REST-PRICE", "Idli", BigDecimal.ZERO, FoodCategory.VEG
        ), "Should reject zero price");

        // Negative price
        assertThrows(ValidationException.class, () -> new MenuItem(
                "ITEM-NEG", "REST-PRICE", "Vada", new BigDecimal("-20.00"), FoodCategory.VEG
        ), "Should reject negative price");

        // Update to non-positive price
        MenuItem item = new MenuItem("ITEM-VAL", "REST-PRICE", "Dosa", new BigDecimal("70.00"), FoodCategory.VEG);
        menuService.addMenuItem(item, UserRole.ADMIN);

        assertThrows(ValidationException.class, () ->
                menuService.updateItemPrice("ITEM-VAL", BigDecimal.ZERO, UserRole.ADMIN),
                "Should reject updating price to zero");
        assertThrows(ValidationException.class, () ->
                menuService.updateItemPrice("ITEM-VAL", new BigDecimal("-5.00"), UserRole.ADMIN),
                "Should reject updating price to negative");
    }

    // --- TEST 10 ---
    public void test10_rejectUnauthorizedMenuChanges() {
        Restaurant r = new Restaurant(
                "REST-AUTH", "Burger Point", CuisineType.FAST_FOOD, "Noida",
                LocalTime.of(10, 0), LocalTime.of(22, 0), new BigDecimal("100.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        MenuItem item = new MenuItem("ITEM-AUTH", "REST-AUTH", "Cheese Burger", new BigDecimal("150.00"), FoodCategory.NON_VEG);

        // Customer cannot add menu item
        assertThrows(UnauthorizedException.class, () -> menuService.addMenuItem(item, UserRole.CUSTOMER),
                "Customer cannot add menu item");

        // Add as admin first
        menuService.addMenuItem(item, UserRole.ADMIN);

        // Customer cannot modify name, price, category, availability, or remove
        assertThrows(UnauthorizedException.class, () -> menuService.updateItemName("ITEM-AUTH", "Veggie Burger", UserRole.CUSTOMER),
                "Customer cannot update name");
        assertThrows(UnauthorizedException.class, () -> menuService.updateItemPrice("ITEM-AUTH", new BigDecimal("160"), UserRole.CUSTOMER),
                "Customer cannot update price");
        assertThrows(UnauthorizedException.class, () -> menuService.updateItemCategory("ITEM-AUTH", FoodCategory.VEG, UserRole.CUSTOMER),
                "Customer cannot update category");
        assertThrows(UnauthorizedException.class, () -> menuService.markItemUnavailable("ITEM-AUTH", UserRole.CUSTOMER),
                "Customer cannot mark unavailable");
        assertThrows(UnauthorizedException.class, () -> menuService.removeMenuItem("ITEM-AUTH", UserRole.CUSTOMER),
                "Customer cannot remove item");
    }

    // --- TEST 11 ---
    public void test11_preventUnavailableItemsFromPassingAvailabilityValidation() {
        Restaurant r = new Restaurant(
                "REST-CART", "Tandoori Nights", CuisineType.NORTH_INDIAN, "Jaipur",
                LocalTime.of(12, 0), LocalTime.of(23, 0), new BigDecimal("100.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        MenuItem item = new MenuItem("ITEM-CART", "REST-CART", "Paneer Tikka", new BigDecimal("220.00"), FoodCategory.VEG, true);
        menuService.addMenuItem(item, UserRole.ADMIN);

        // Available initially
        assertTrue(menuService.isAvailable("ITEM-CART"), "Item should be available initially");
        menuService.validateItemForCart("ITEM-CART"); // Must succeed without exception

        // Mark unavailable
        menuService.markItemUnavailable("ITEM-CART", UserRole.ADMIN);
        assertFalse(menuService.isAvailable("ITEM-CART"), "Item should be reported as unavailable");

        // Cart validation must reject unavailable item
        assertThrows(ValidationException.class, () -> menuService.validateItemForCart("ITEM-CART"),
                "Cart validation must fail for unavailable item");
    }

    // --- TEST 12 ---
    public void test12_preventRestaurantDeletionWhenActiveOrdersExist() {
        Restaurant r = new Restaurant(
                "REST-DEL-ACT", "Pizza Palace", CuisineType.ITALIAN, "Gurgaon",
                LocalTime.of(11, 0), LocalTime.of(23, 0), new BigDecimal("200.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        // Case A: ActiveOrderChecker is not configured -> must NOT silently allow deletion
        restaurantService.setActiveOrderChecker(null);
        assertThrows(RestaurantStateException.class, () -> restaurantService.deleteRestaurant("REST-DEL-ACT", UserRole.ADMIN),
                "Deletion must fail when active order status cannot be verified");

        // Case B: ActiveOrderChecker reports active orders
        ActiveOrderChecker activeChecker = restaurantId -> "REST-DEL-ACT".equals(restaurantId);
        restaurantService.setActiveOrderChecker(activeChecker);

        assertThrows(RestaurantStateException.class, () -> restaurantService.deleteRestaurant("REST-DEL-ACT", UserRole.ADMIN),
                "Deletion must fail when active orders exist");

        // Restaurant must still exist in repo
        assertTrue(restaurantRepo.existsById("REST-DEL-ACT"), "Restaurant should still exist after rejected deletion");
    }

    // --- TEST 13 ---
    public void test13_allowDeletionWhenNoActiveOrdersExist() {
        Restaurant r = new Restaurant(
                "REST-DEL-OK", "Salad Bowl", CuisineType.FAST_FOOD, "Goa",
                LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("80.00")
        );
        restaurantService.addRestaurant(r, UserRole.ADMIN);

        // Add menu items to restaurant
        MenuItem m1 = new MenuItem("ITEM-DEL-1", "REST-DEL-OK", "Greek Salad", new BigDecimal("150.00"), FoodCategory.VEG);
        menuService.addMenuItem(m1, UserRole.ADMIN);

        // Configure checker returning false (no active orders)
        ActiveOrderChecker noActiveOrdersChecker = restaurantId -> false;
        restaurantService.setActiveOrderChecker(noActiveOrdersChecker);

        // Perform deletion
        restaurantService.deleteRestaurant("REST-DEL-OK", UserRole.ADMIN);

        // Verify restaurant is deleted
        assertFalse(restaurantRepo.existsById("REST-DEL-OK"), "Restaurant must be deleted from repository");
        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById("REST-DEL-OK"),
                "Fetching deleted restaurant must throw ResourceNotFoundException");

        // Verify menu items belonging to the restaurant are also cleaned up
        List<MenuItem> items = menuService.getMenuItemsByRestaurantId("REST-DEL-OK");
        assertTrue(items.isEmpty(), "Menu items for deleted restaurant must be removed");
    }

    // --- TEST 14 ---
    public void test14_rejectDuplicateIds() {
        Restaurant r1 = new Restaurant("REST-DUP", "Diner A", CuisineType.FAST_FOOD, "Delhi", LocalTime.of(9, 0), LocalTime.of(21, 0), new BigDecimal("100"));
        Restaurant r2 = new Restaurant("REST-DUP", "Diner B", CuisineType.CHINESE, "Mumbai", LocalTime.of(10, 0), LocalTime.of(22, 0), new BigDecimal("150"));

        restaurantService.addRestaurant(r1, UserRole.ADMIN);
        assertThrows(DuplicateResourceException.class, () -> restaurantService.addRestaurant(r2, UserRole.ADMIN),
                "Duplicate restaurant ID must be rejected");

        MenuItem m1 = new MenuItem("ITEM-DUP", "REST-DUP", "Fries", new BigDecimal("50"), FoodCategory.VEG);
        MenuItem m2 = new MenuItem("ITEM-DUP", "REST-DUP", "Loaded Fries", new BigDecimal("90"), FoodCategory.VEG);

        menuService.addMenuItem(m1, UserRole.ADMIN);
        assertThrows(DuplicateResourceException.class, () -> menuService.addMenuItem(m2, UserRole.ADMIN),
                "Duplicate menu item ID must be rejected");
    }

    // --- TEST 15 ---
    public void test15_verifyRetrievalFromRepository() {
        Restaurant r1 = new Restaurant("REST-R1", "Kitchen One", CuisineType.NORTH_INDIAN, "Delhi", LocalTime.of(10, 0), LocalTime.of(22, 0), new BigDecimal("100"));
        Restaurant r2 = new Restaurant("REST-R2", "Kitchen Two", CuisineType.SOUTH_INDIAN, "Bangalore", LocalTime.of(8, 0), LocalTime.of(20, 0), new BigDecimal("50"));
        restaurantService.addRestaurant(r1, UserRole.ADMIN);
        restaurantService.addRestaurant(r2, UserRole.ADMIN);

        List<Restaurant> all = restaurantService.listRestaurants();
        assertTrue(all.size() >= 2, "All restaurants list contains added items");

        Restaurant fetched1 = restaurantService.getRestaurantById("REST-R1");
        assertEquals("Kitchen One", fetched1.getName(), "Restaurant 1 retrieved correctly");

        MenuItem m1 = new MenuItem("M-101", "REST-R1", "Butter Naan", new BigDecimal("40.00"), FoodCategory.VEG);
        MenuItem m2 = new MenuItem("M-102", "REST-R1", "Garlic Naan", new BigDecimal("50.00"), FoodCategory.VEG);
        MenuItem m3 = new MenuItem("M-103", "REST-R1", "Paneer Butter Masala", new BigDecimal("200.00"), FoodCategory.VEG);
        menuService.addMenuItem(m1, UserRole.ADMIN);
        menuService.addMenuItem(m2, UserRole.ADMIN);
        menuService.addMenuItem(m3, UserRole.ADMIN);

        List<MenuItem> r1Items = menuService.getMenuItemsByRestaurantId("REST-R1");
        assertEquals(3, r1Items.size(), "Restaurant 1 has 3 items");

        List<MenuItem> searchResults = menuService.searchMenuItems("REST-R1", "naan");
        assertEquals(2, searchResults.size(), "Search for 'naan' returns 2 items");

        assertEquals(0, new BigDecimal("40.00").compareTo(menuService.getItemPrice("M-101")), "Price retrieved correctly");
    }

    // --- Assertion Utilities ---

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion Failed (expected false, got true): " + message);
        }
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
            throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but no exception was thrown. " + message);
        } catch (Throwable t) {
            if (!expectedException.isInstance(t)) {
                throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage() + ". " + message, t);
            }
        }
    }
}

