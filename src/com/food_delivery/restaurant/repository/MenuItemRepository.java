package com.food_delivery.restaurant.repository;

import com.food_delivery.restaurant.model.MenuItem;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for persisting and querying MenuItem entities.
 */
public interface MenuItemRepository {

    /**
     * Saves or updates a menu item.
     *
     * @param menuItem the menu item to save
     * @return the saved menu item
     */
    MenuItem save(MenuItem menuItem);

    /**
     * Finds a menu item by its unique identifier.
     *
     * @param id menu item ID
     * @return an Optional containing the menu item if found, or empty Optional
     */
    Optional<MenuItem> findById(String id);

    /**
     * Retrieves all menu items belonging to a specific restaurant.
     *
     * @param restaurantId restaurant ID
     * @return list of menu items
     */
    List<MenuItem> findByRestaurantId(String restaurantId);

    /**
     * Searches menu items within a restaurant by name (case-insensitive substring).
     *
     * @param restaurantId restaurant ID
     * @param query search query
     * @return matching menu items
     */
    List<MenuItem> searchByName(String restaurantId, String query);

    /**
     * Checks if a menu item exists with the given ID.
     *
     * @param id menu item ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes a menu item by its ID.
     *
     * @param id menu item ID
     */
    void deleteById(String id);

    /**
     * Deletes all menu items belonging to the specified restaurant.
     *
     * @param restaurantId restaurant ID
     */
    void deleteByRestaurantId(String restaurantId);

    /**
     * Clears all stored menu items (useful for test resets).
     */
    void clear();
}

