package com.food_delivery.cart.dto;

/**
 * Data Transfer Object for adding a menu item to a customer's cart.
 */
public class AddToCartRequest {
    private String customerId;
    private String restaurantId;
    private String menuItemId;
    private int quantity;

    public AddToCartRequest() {
    }

    public AddToCartRequest(String customerId, String restaurantId, String menuItemId, int quantity) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
