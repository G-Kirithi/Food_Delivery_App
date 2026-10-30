package com.food_delivery.cart.exception;

/**
 * Base unchecked exception for cart domain errors.
 */
public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
