package com.food_delivery.cart.dto;

/**
 * Data Transfer Object for updating an existing cart item's quantity.
 */
public class UpdateCartItemRequest {
    private int quantity;

    public UpdateCartItemRequest() {
    }

    public UpdateCartItemRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
