# Food_Delivery_App

A collaborative Low-Level Design (LLD) project for an online food delivery platform developed in Java.

---

## Module 1: Restaurant and Menu Management

### 1. Architecture and Package Layout

The module follows a layered, decoupled architecture under package `com.food_delivery.restaurant`:

```text
src/com/food_delivery/restaurant/
├── enums/
│   ├── CuisineType.java           # NORTH_INDIAN, SOUTH_INDIAN, CHINESE, ITALIAN, FAST_FOOD
│   ├── FoodCategory.java          # VEG, NON_VEG
│   └── UserRole.java              # ADMIN, RESTAURANT_OWNER, CUSTOMER
├── exception/
│   ├── RestaurantException.java           # Base domain runtime exception
│   ├── ValidationException.java           # Missing/blank fields, invalid values
│   ├── UnauthorizedException.java         # Insufficient permissions
│   ├── ResourceNotFoundException.java     # Restaurant/menu item not found
│   ├── DuplicateResourceException.java    # Duplicate ID collisions
│   └── RestaurantStateException.java      # Mutating protected fields, deleting with active orders
├── model/
│   ├── Restaurant.java            # Restaurant entity with schedule logic & validation
│   └── MenuItem.java              # Menu item entity with price & category validation
├── repository/
│   ├── RestaurantRepository.java          # Interface for restaurant persistence
│   ├── InMemoryRestaurantRepository.java  # Thread-safe ConcurrentHashMap storage
│   ├── MenuItemRepository.java            # Interface for menu item persistence
│   └── InMemoryMenuItemRepository.java    # Thread-safe ConcurrentHashMap storage
├── service/
│   ├── RestaurantService.java             # Contract for restaurant operations
│   ├── RestaurantServiceImpl.java         # Restaurant business logic & validation
│   ├── MenuService.java                   # Contract for menu operations & cart checks
│   └── MenuServiceImpl.java               # Menu business logic & validation
├── integration/
│   └── ActiveOrderChecker.java            # Interface for checking active orders (Order module integration)
└── test/
    └── RestaurantTestSuite.java           # 15 deterministic unit and integration test cases
```

---

### 2. Classes and Responsibilities

| Class / Interface | Responsibility |
|---|---|
| `Restaurant` | Represents a restaurant entity. Enforces invariants (non-blank strings, non-negative minimum order value, valid times), tracks activation & open status, protects immutable fields after activation, and calculates order acceptance including overnight operating schedules. |
| `MenuItem` | Represents a dish belonging to a restaurant. Enforces non-blank name, positive price (`BigDecimal`), valid `FoodCategory`, and availability status. |
| `CuisineType` | Enum of supported cuisines: `NORTH_INDIAN`, `SOUTH_INDIAN`, `CHINESE`, `ITALIAN`, `FAST_FOOD`. |
| `FoodCategory` | Enum of menu categories: `VEG`, `NON_VEG`. |
| `UserRole` | Enum of user roles: `ADMIN`, `RESTAURANT_OWNER`, `CUSTOMER`. |
| `RestaurantRepository` | Interface defining restaurant persistence operations (`save`, `findById`, `findAll`, `existsById`, `deleteById`). |
| `InMemoryRestaurantRepository` | In-memory thread-safe implementation of `RestaurantRepository` using `ConcurrentHashMap`. |
| `MenuItemRepository` | Interface defining menu item queries (`save`, `findById`, `findByRestaurantId`, `searchByName`, `deleteById`, `deleteByRestaurantId`). |
| `InMemoryMenuItemRepository` | In-memory thread-safe implementation of `MenuItemRepository` using `ConcurrentHashMap`. |
| `RestaurantService` / `Impl` | Coordinates restaurant creation, activation, operating hours, status toggling, deletion with active order checks, and query methods. |
| `MenuService` / `Impl` | Coordinates menu item creation, modifications, availability toggles, search, and cart validation. |
| `ActiveOrderChecker` | Decoupled integration contract that the Order Management module implements to check if orders other than `DELIVERED` or `CANCELLED` exist. |

---

### 3. How Restaurant Creation Works

1. Call `restaurantService.addRestaurant(restaurant, userRole)`.
2. **Authorization check:** The caller's `userRole` must be `ADMIN` or `RESTAURANT_OWNER`. A `CUSTOMER` role or `null` triggers an `UnauthorizedException`.
3. **Validation:** Checks that `id`, `name`, `cuisineType`, `city`, `openingTime`, `closingTime`, and `minimumOrderValue` are non-null and valid. Non-negative minimum order values are required.
4. **Uniqueness:** Checks `restaurantRepo.existsById(restaurant.getId())`. Duplicate IDs throw `DuplicateResourceException`.
5. Newly created restaurants start with `isActive = false` and `isOpen = false`.
6. Once activated via `activateRestaurant(id, role)`, the restaurant's `name`, `cuisineType`, and `city` become permanently immutable. Attempting to modify them throws `RestaurantStateException`.
7. Operating hours and minimum order values can be safely updated before or after activation.

---

### 4. How Menu Management Works

1. Call `menuService.addMenuItem(menuItem, userRole)`.
2. **Authorization check:** Only `ADMIN` or `RESTAURANT_OWNER` can add, modify, or delete menu items.
3. **Parent restaurant validation:** Verifies that the associated `restaurantId` exists in the system; otherwise throws `ResourceNotFoundException`.
4. **Price validation:** Prices use `BigDecimal` and must be strictly greater than zero (`price > 0`).
5. **Uniqueness:** Rejects duplicate menu item IDs with `DuplicateResourceException`.
6. **Availability toggling:** Managers can toggle availability via `markItemAvailable` or `markItemUnavailable`.
7. **Deletion:** Removing an item deletes it from the repository; deleting a restaurant cascades removal to all of its menu items.

---

### 5. How Authorization is Checked

Every state-mutating method on `RestaurantService` and `MenuService` accepts a `UserRole role` argument:

```java
private void checkAuthorizedAdminOrOwner(UserRole role, String action) {
    if (role == null) {
        throw new UnauthorizedException("User role cannot be null. Authorization failed to " + action + ".");
    }
    if (role == UserRole.CUSTOMER) {
        throw new UnauthorizedException("User with role CUSTOMER is not authorized to " + action + ".");
    }
    if (role != UserRole.ADMIN && role != UserRole.RESTAURANT_OWNER) {
        throw new UnauthorizedException("Unauthorized role '" + role + "' cannot " + action + ".");
    }
}
```

Read/query operations (such as listing menus, retrieving details, checking availability, and validating items for cart) are public and do not require administrative privileges.

---

### 6. Integration Guide for Other Modules

Other modules (Cart, Order, Delivery) interact through clean, read-safe service methods:

#### A. Order Module Integration
- **Checking if a restaurant is accepting orders:**
  ```java
  boolean accepting = restaurantService.isAcceptingOrders(restaurantId, currentTime);
  ```
  Returns `true` only when:
  1. The restaurant is activated (`isActive == true`).
  2. The restaurant is marked open (`isOpen == true`).
  3. `currentTime` falls within the restaurant's operating hours (fully supports overnight schedules that cross midnight, e.g., 20:00 to 02:00).
- **Verifying minimum order value:**
  ```java
  BigDecimal minOrderValue = restaurantService.getMinimumOrderValue(restaurantId);
  ```
- **Implementing the `ActiveOrderChecker` contract for safe deletion:**
  ```java
  public class OrderModuleActiveOrderChecker implements ActiveOrderChecker {
      private final OrderRepository orderRepo;

      @Override
      public boolean hasActiveOrders(String restaurantId) {
          // Return true if any orders exist with status OTHER than DELIVERED or CANCELLED
          return orderRepo.hasNonTerminalOrders(restaurantId);
      }
  }

  // Register with restaurant service:
  restaurantService.setActiveOrderChecker(new OrderModuleActiveOrderChecker());
  ```
  *Note:* Restaurant deletion is strictly blocked if `ActiveOrderChecker` is unconfigured or reports active orders.

#### B. Cart Module Integration
- **Checking item availability:**
  ```java
  boolean isAvailable = menuService.isAvailable(menuItemId);
  ```
- **Validating item prior to adding to cart:**
  ```java
  menuService.validateItemForCart(menuItemId);
  ```
  Throws `ValidationException` if the item is marked unavailable, or `ResourceNotFoundException` if the item ID does not exist.
- **Fetching item price:**
  ```java
  BigDecimal price = menuService.getItemPrice(menuItemId);
  ```

---

### 7. How to Run the Build and Tests

Since the project uses standard Java without third-party build tools:

#### Using PowerShell / Windows Command Line:
```powershell
# 1. Compile all Java source files into the bin directory
javac -d bin (Get-ChildItem -Recurse -Filter *.java -Path src | Select-Object -ExpandProperty FullName)

# 2. Run the deterministic test suite
java -cp bin com.food_delivery.restaurant.test.RestaurantTestSuite
```

#### Test Suite Coverage (15 Scenarios):
1. Successfully create a valid restaurant.
2. Reject invalid restaurants (missing/blank name, missing city, null cuisine, invalid times, negative minimum order value).
3. Reject unauthorized restaurant creation (CUSTOMER role or null).
4. Prevent changes to name, cuisine, and city after activation (both service and model layer).
5. Allow permitted timing and minimum-order-value updates after activation.
6. Correctly determine whether a restaurant is accepting orders (active, open, within hours).
7. Correctly handle opening hours crossing midnight (e.g., 20:00 to 02:00 tested across day and night boundaries).
8. Successfully add a valid menu item.
9. Reject a menu item with a zero or negative price.
10. Reject unauthorized menu modifications by CUSTOMER role.
11. Prevent unavailable items from passing cart availability validation.
12. Prevent restaurant deletion when active orders exist or when active order checker is not configured.
13. Allow deletion when no active orders exist, and cascade cleanup of menu items.
14. Reject duplicate restaurant and menu item IDs.
15. Verify menu items and restaurants are correctly retrieved and searchable from repositories.

---

## Module 2: Customer and Cart Management

### 1. Architecture and Package Layout

Module 2 follows domain-driven package layout under `com.food_delivery.customer` and `com.food_delivery.cart`:

```text
src/com/food_delivery/
├── common/
│   └── api/
│       └── ApiResponse.java               # Standard HTTP-compatible response wrapper
├── customer/
│   ├── controller/
│   │   └── CustomerController.java        # REST API endpoints for customer & address
│   ├── dto/
│   │   ├── AddressRequest.java            # DTO for adding/updating addresses
│   │   └── CustomerRegistrationRequest.java # DTO for customer registration
│   ├── exception/
│   │   ├── CustomerException.java         # Base domain exception
│   │   ├── CustomerNotFoundException.java # 404 Not Found
│   │   ├── CustomerValidationException.java # 400 Bad Request
│   │   ├── DuplicatePhoneNumberException.java # 409 Conflict
│   │   └── InvalidAddressException.java   # 400 Bad Request (including last address deletion)
│   ├── model/
│   │   ├── Address.java                   # Address entity with invariant validation
│   │   └── Customer.java                  # Customer entity with address collection invariants
│   ├── repository/
│   │   ├── CustomerRepository.java        # Interface for customer persistence
│   │   └── InMemoryCustomerRepository.java # Thread-safe ConcurrentHashMap storage
│   ├── service/
│   │   ├── CustomerService.java           # Interface for customer operations
│   │   └── CustomerServiceImpl.java       # Customer business logic & validations
│   └── test/
│       └── CustomerTestSuite.java         # 11 deterministic unit test cases
├── cart/
│   ├── controller/
│   │   └── CartController.java            # REST API endpoints for cart operations
│   ├── dto/
│   │   ├── AddToCartRequest.java          # DTO for adding items to cart
│   │   ├── UpdateCartItemRequest.java     # DTO for updating item quantity
│   │   ├── CartItemResponse.java          # DTO for cart item responses
│   │   └── CartResponse.java              # DTO for complete cart response
│   ├── exception/
│   │   ├── CartException.java             # Base domain exception
│   │   ├── CartNotFoundException.java     # 404 Not Found
│   │   ├── InvalidQuantityException.java  # 400 Bad Request (quantity < 1 or > 10)
│   │   ├── MenuItemUnavailableException.java # 400 Bad Request (item marked unavailable)
│   │   └── RestaurantMismatchException.java # 409 Conflict (items from multiple restaurants)
│   ├── model/
│   │   ├── Cart.java                      # Cart entity with single-restaurant enforcement
│   │   └── CartItem.java                  # Line item with subtotal calculation
│   ├── repository/
│   │   ├── CartRepository.java            # Interface for cart persistence
│   │   └── InMemoryCartRepository.java    # Thread-safe ConcurrentHashMap storage
│   ├── service/
│   │   ├── CartService.java               # Interface for cart operations
│   │   └── CartServiceImpl.java           # Cart business logic & Module 1 integration
│   └── test/
│       └── CartTestSuite.java             # 18 deterministic unit and business rule test cases
├── integration/
│   └── test/
│       └── Module2IntegrationTestSuite.java # 8 end-to-end integration test cases
└── test/
    └── AllModulesTestSuite.java           # Master runner executing all 52 test cases
```

### 2. How to Run the Build and Tests

#### Run All Tests (52 Tests across Module 1 & Module 2):
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java -Path src | Select-Object -ExpandProperty FullName)
java -cp bin com.food_delivery.test.AllModulesTestSuite
```

#### Run Module 2 Tests Individually:
```powershell
# Customer tests (11 tests)
java -cp bin com.food_delivery.customer.test.CustomerTestSuite

# Cart tests (18 tests)
java -cp bin com.food_delivery.cart.test.CartTestSuite

# Module 1 & 2 Integration tests (8 tests)
java -cp bin com.food_delivery.integration.test.Module2IntegrationTestSuite
```

