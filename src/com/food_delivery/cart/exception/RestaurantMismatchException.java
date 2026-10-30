package com.food_delivery.cart.exception;

/**
 * Thrown when attempting to add items from multiple restaurants to the same cart.
 */
public class RestaurantMismatchException extends CartException {
    public RestaurantMismatchException(String message) {
        super(message);
    }
}
