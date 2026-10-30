package com.food_delivery.order.exception;

/**
 * Thrown when an order with the requested ID is not found.
 */
public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
