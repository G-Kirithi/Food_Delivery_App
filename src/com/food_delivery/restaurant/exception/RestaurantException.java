package com.food_delivery.restaurant.exception;

/**
 * Base unchecked exception for all restaurant and menu management domain errors.
 */
public class RestaurantException extends RuntimeException {
    public RestaurantException(String message) {
        super(message);
    }

    public RestaurantException(String message, Throwable cause) {
        super(message, cause);
    }
}

