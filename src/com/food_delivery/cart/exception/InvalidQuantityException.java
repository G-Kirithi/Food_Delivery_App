package com.food_delivery.cart.exception;

/**
 * Thrown when an item quantity is non-positive or exceeds maximum allowed limit (10).
 */
public class InvalidQuantityException extends CartException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
