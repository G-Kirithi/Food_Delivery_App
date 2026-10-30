package com.food_delivery.restaurant.service;

import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.model.MenuItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Menu Item management and availability queries.
 */
public interface MenuService {

    /**
     * Adds a new menu item to a restaurant.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     * The restaurant must exist.
     *
     * @param menuItem the menu item entity
     * @param role     caller role
     * @return the created menu item
     */
    MenuItem addMenuItem(MenuItem menuItem, UserRole role);

    /**
     * Retrieves a menu item by ID.
     *
     * @param menuItemId menu item ID
     * @return the menu item
     */
    MenuItem getMenuItemById(String menuItemId);

    /**
     * Lists all menu items for a specific restaurant.
     *
     * @param restaurantId restaurant ID
     * @return list of menu items
     */
    List<MenuItem> getMenuItemsByRestaurantId(String restaurantId);

    /**
     * Updates an item's name.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId menu item ID
     * @param newName    new name
     * @param role       caller role
     * @return updated menu item
     */
    MenuItem updateItemName(String menuItemId, String newName, UserRole role);

    /**
     * Updates an item's price.
     * Price must be greater than zero.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId menu item ID
     * @param newPrice   new price
     * @param role       caller role
     * @return updated menu item
     */
    MenuItem updateItemPrice(String menuItemId, BigDecimal newPrice, UserRole role);

    /**
     * Updates an item's category (VEG / NON_VEG).
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId  menu item ID
     * @param newCategory new food category
     * @param role        caller role
     * @return updated menu item
     */
    MenuItem updateItemCategory(String menuItemId, FoodCategory newCategory, UserRole role);

    /**
     * Marks a menu item as Available.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId menu item ID
     * @param role       caller role
     * @return updated menu item
     */
    MenuItem markItemAvailable(String menuItemId, UserRole role);

    /**
     * Marks a menu item as Unavailable.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId menu item ID
     * @param role       caller role
     * @return updated menu item
     */
    MenuItem markItemUnavailable(String menuItemId, UserRole role);

    /**
     * Removes a menu item from the restaurant.
     * Allowed only for ADMIN or RESTAURANT_OWNER.
     *
     * @param menuItemId menu item ID
     * @param role       caller role
     */
    void removeMenuItem(String menuItemId, UserRole role);

    /**
     * Searches menu items within a restaurant by name.
     *
     * @param restaurantId restaurant ID
     * @param query        search query
     * @return list of matching menu items
     */
    List<MenuItem> searchMenuItems(String restaurantId, String query);

    /**
     * Checks if a menu item is available for ordering.
     *
     * @param menuItemId menu item ID
     * @return true if available, false if unavailable or not found
     */
    boolean isAvailable(String menuItemId);

    /**
     * Retrieves the price of a menu item.
     *
     * @param menuItemId menu item ID
     * @return item price
     */
    BigDecimal getItemPrice(String menuItemId);

    /**
     * Validates whether a menu item can be added to a cart.
     * Rejects unavailable items or non-existent items by throwing a ValidationException
     * or ResourceNotFoundException.
     *
     * @param menuItemId menu item ID
     */
    void validateItemForCart(String menuItemId);
}

