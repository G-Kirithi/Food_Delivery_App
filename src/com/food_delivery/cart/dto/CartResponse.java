package com.food_delivery.cart.dto;

import com.food_delivery.cart.model.Cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object representing the customer's Cart in API responses.
 */
public class CartResponse {
    private String id;
    private String customerId;
    private String restaurantId;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponse() {
    }

    public CartResponse(Cart cart) {
        if (cart != null) {
            this.id = cart.getId();
            this.customerId = cart.getCustomerId();
            this.restaurantId = cart.getRestaurantId();
            this.items = cart.getItems().stream()
                    .map(CartItemResponse::new)
                    .collect(Collectors.toList());
            this.totalAmount = cart.getTotalAmount();
            this.createdAt = cart.getCreatedAt();
            this.updatedAt = cart.getUpdatedAt();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
