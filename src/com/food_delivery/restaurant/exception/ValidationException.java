package com.food_delivery.restaurant.exception;

/**
 * Thrown when input validation fails for restaurant or menu operations.
 */
public class ValidationException extends RestaurantException {
    public ValidationException(String message) {
        super(message);
    }
}

