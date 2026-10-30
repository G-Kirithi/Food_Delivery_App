package com.food_delivery.order.exception;

/**
 * Base unchecked exception for all order management domain errors.
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
