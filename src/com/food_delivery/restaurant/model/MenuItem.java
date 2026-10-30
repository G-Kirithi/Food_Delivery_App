package com.food_delivery.restaurant.model;

import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Model representing a Menu Item belonging to a Restaurant.
 */
public class MenuItem {
    private final String id;
    private final String restaurantId;
    private String name;
    private BigDecimal price;
    private FoodCategory category;
    private boolean isAvailable;

    public MenuItem(String id,
                    String restaurantId,
                    String name,
                    BigDecimal price,
                    FoodCategory category,
                    boolean isAvailable) {
        validateId(id);
        validateRestaurantId(restaurantId);
        validateName(name);
        validatePrice(price);
        validateCategory(category);

        this.id = id.trim();
        this.restaurantId = restaurantId.trim();
        this.name = name.trim();
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
    }

    public MenuItem(String id,
                    String restaurantId,
                    String name,
                    BigDecimal price,
                    FoodCategory category) {
        this(id, restaurantId, name, price, category, true);
    }

    public String getId() {
        return id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        validatePrice(price);
        this.price = price;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        validateCategory(category);
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // --- Validation Helpers ---

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Menu item ID cannot be null or blank.");
        }
    }

    private void validateRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank for menu item.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Menu item name cannot be null or blank.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Menu item price cannot be null.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Menu item price must be greater than zero. Provided: " + price);
        }
    }

    private void validateCategory(FoodCategory category) {
        if (category == null) {
            throw new ValidationException("Menu item category cannot be null.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", isAvailable=" + isAvailable +
                '}';
    }
}

