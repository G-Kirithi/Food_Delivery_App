package com.food_delivery.restaurant.exception;

/**
 * Thrown when an illegal state transition or protected field modification is attempted.
 */
public class RestaurantStateException extends RestaurantException {
    public RestaurantStateException(String message) {
        super(message);
    }
}

