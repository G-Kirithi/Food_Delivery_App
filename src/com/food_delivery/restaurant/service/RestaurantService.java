package com.food_delivery.restaurant.service;

import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.integration.ActiveOrderChecker;
import com.food_delivery.restaurant.model.Restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for Restaurant management operations.
 */
public interface RestaurantService {

    /**
     * Registers a new restaurant.
     * Allowed only for ADMIN or RESTAURANT_OWNER roles.
     *
     * @param restaurant the restaurant entity to create
     * @param role       the role of the user attempting creation
     * @return the created restaurant
     */
    Restaurant addRestaurant(Restaurant restaurant, UserRole role);

    /**
     * Retrieves a restaurant by its unique ID.
     *
     * @param id restaurant ID
     * @return the found restaurant
     */
    Restaurant getRestaurantById(String id);

    /**
     * Lists all restaurants registered in the system.
     *
     * @return list of all restaurants
     */
    List<Restaurant> listRestaurants();

    /**
     * Activates a restaurant.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     * Once activated, name, cuisine, and city cannot be changed.
     *
     * @param id   restaurant ID
     * @param role caller role
     * @return the activated restaurant
     */
    Restaurant activateRestaurant(String id, UserRole role);

    /**
     * Marks a restaurant as Open.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id   restaurant ID
     * @param role caller role
     * @return the updated restaurant
     */
    Restaurant markOpen(String id, UserRole role);

    /**
     * Marks a restaurant as Closed.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id   restaurant ID
     * @param role caller role
     * @return the updated restaurant
     */
    Restaurant markClosed(String id, UserRole role);

    /**
     * Updates the opening and closing times of a restaurant.
     * Permitted before and after activation.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id          restaurant ID
     * @param openingTime new opening time
     * @param closingTime new closing time
     * @param role        caller role
     * @return the updated restaurant
     */
    Restaurant updateOperatingHours(String id, LocalTime openingTime, LocalTime closingTime, UserRole role);

    /**
     * Updates the minimum order value of a restaurant.
     * Permitted before and after activation.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id                restaurant ID
     * @param minimumOrderValue new non-negative minimum order value
     * @param role              caller role
     * @return the updated restaurant
     */
    Restaurant updateMinimumOrderValue(String id, BigDecimal minimumOrderValue, UserRole role);

    /**
     * Updates restaurant core details (name, cuisine, city).
     * Disallowed once the restaurant is activated.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id      restaurant ID
     * @param name    new restaurant name
     * @param cuisine new cuisine type
     * @param city    new city
     * @param role    caller role
     * @return the updated restaurant
     */
    Restaurant updateRestaurantDetails(String id, String name, CuisineType cuisine, String city, UserRole role);

    /**
     * Deletes a restaurant from the system.
     * Permitted only if the restaurant has NO active orders.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param id   restaurant ID
     * @param role caller role
     */
    void deleteRestaurant(String id, UserRole role);

    /**
     * Checks if a restaurant is currently accepting orders.
     * A restaurant accepts orders if and only if:
     * - It is activated
     * - It is marked as Open
     * - The given currentTime is within operating hours (supports midnight crossing)
     *
     * @param id          restaurant ID
     * @param currentTime the time to evaluate
     * @return true if accepting orders, false otherwise
     */
    boolean isAcceptingOrders(String id, LocalTime currentTime);

    /**
     * Retrieves the minimum order value for a restaurant.
     *
     * @param id restaurant ID
     * @return minimum order value
     */
    BigDecimal getMinimumOrderValue(String id);

    /**
     * Checks if a restaurant is activated.
     *
     * @param id restaurant ID
     * @return true if active, false otherwise
     */
    boolean isRestaurantActive(String id);

    /**
     * Checks if a restaurant is marked Open.
     *
     * @param id restaurant ID
     * @return true if open, false otherwise
     */
    boolean isRestaurantOpen(String id);

    /**
     * Configures the ActiveOrderChecker integration dependency.
     *
     * @param activeOrderChecker checker instance
     */
    void setActiveOrderChecker(ActiveOrderChecker activeOrderChecker);
}

