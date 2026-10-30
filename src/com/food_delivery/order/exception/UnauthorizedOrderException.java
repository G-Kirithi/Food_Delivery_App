package com.food_delivery.order.exception;

/**
 * Thrown when a caller attempts an order operation without required permissions
 * (e.g., non-owner accepting order, customer attempting restaurant state changes).
 */
public class UnauthorizedOrderException extends OrderException {
    public UnauthorizedOrderException(String message) {
        super(message);
    }
}
