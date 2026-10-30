package com.food_delivery.order.exception;

/**
 * Thrown when an order fails business rule pre-validation (e.g. restaurant closed,
 * minimum order value not met, unserviceable city, empty cart, active order limit exceeded).
 */
public class OrderValidationException extends OrderException {
    public OrderValidationException(String message) {
        super(message);
    }
}
