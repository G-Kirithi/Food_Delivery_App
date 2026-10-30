package com.food_delivery.restaurant.service;

import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.DuplicateResourceException;
import com.food_delivery.restaurant.exception.ResourceNotFoundException;
import com.food_delivery.restaurant.exception.UnauthorizedException;
import com.food_delivery.restaurant.exception.ValidationException;
import com.food_delivery.restaurant.model.MenuItem;
import com.food_delivery.restaurant.repository.MenuItemRepository;
import com.food_delivery.restaurant.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Default implementation of MenuService containing business logic for Menu Management.
 */
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuServiceImpl(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        if (menuItemRepository == null) {
            throw new IllegalArgumentException("MenuItemRepository cannot be null.");
        }
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("RestaurantRepository cannot be null.");
        }
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public MenuItem addMenuItem(MenuItem menuItem, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "add a menu item");
        if (menuItem == null) {
            throw new ValidationException("Menu item entity cannot be null.");
        }

        if (!restaurantRepository.existsById(menuItem.getRestaurantId())) {
            throw new ResourceNotFoundException("Cannot add menu item. Restaurant not found with ID: " + menuItem.getRestaurantId());
        }

        if (menuItemRepository.existsById(menuItem.getId())) {
            throw new DuplicateResourceException("Menu item with ID '" + menuItem.getId() + "' already exists.");
        }

        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem getMenuItemById(String menuItemId) {
        if (menuItemId == null || menuItemId.trim().isEmpty()) {
            throw new ValidationException("Menu item ID cannot be null or blank.");
        }
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + menuItemId));
    }

    @Override
    public List<MenuItem> getMenuItemsByRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public MenuItem updateItemName(String menuItemId, String newName, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update menu item name");
        MenuItem item = getMenuItemById(menuItemId);
        item.setName(newName);
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem updateItemPrice(String menuItemId, BigDecimal newPrice, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update menu item price");
        MenuItem item = getMenuItemById(menuItemId);
        item.setPrice(newPrice);
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem updateItemCategory(String menuItemId, FoodCategory newCategory, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update menu item category");
        MenuItem item = getMenuItemById(menuItemId);
        item.setCategory(newCategory);
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem markItemAvailable(String menuItemId, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "mark menu item available");
        MenuItem item = getMenuItemById(menuItemId);
        item.setAvailable(true);
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem markItemUnavailable(String menuItemId, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "mark menu item unavailable");
        MenuItem item = getMenuItemById(menuItemId);
        item.setAvailable(false);
        return menuItemRepository.save(item);
    }

    @Override
    public void removeMenuItem(String menuItemId, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "remove a menu item");
        // Verify existence first
        getMenuItemById(menuItemId);
        menuItemRepository.deleteById(menuItemId);
    }

    @Override
    public List<MenuItem> searchMenuItems(String restaurantId, String query) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
        return menuItemRepository.searchByName(restaurantId, query);
    }

    @Override
    public boolean isAvailable(String menuItemId) {
        if (menuItemId == null || menuItemId.trim().isEmpty()) {
            return false;
        }
        return menuItemRepository.findById(menuItemId)
                .map(MenuItem::isAvailable)
                .orElse(false);
    }

    @Override
    public BigDecimal getItemPrice(String menuItemId) {
        return getMenuItemById(menuItemId).getPrice();
    }

    @Override
    public void validateItemForCart(String menuItemId) {
        MenuItem item = getMenuItemById(menuItemId);
        if (!item.isAvailable()) {
            throw new ValidationException("Menu item '" + item.getName() + "' (ID: " + menuItemId + ") is currently unavailable.");
        }
    }

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
}

