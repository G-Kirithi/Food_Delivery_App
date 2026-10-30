package com.food_delivery.order.dto;

import com.food_delivery.restaurant.exception.ValidationException;

/**
 * Data Transfer Object for placing an order from the customer's active cart.
 */
public class PlaceOrderRequest {
    private String customerId;
    private String deliveryAddressId;

    public PlaceOrderRequest() {
    }

    public PlaceOrderRequest(String customerId, String deliveryAddressId) {
        setCustomerId(customerId);
        setDeliveryAddressId(deliveryAddressId);
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
        this.customerId = customerId.trim();
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        if (deliveryAddressId == null || deliveryAddressId.trim().isEmpty()) {
            throw new ValidationException("Delivery address ID cannot be null or blank.");
        }
        this.deliveryAddressId = deliveryAddressId.trim();
    }
}
