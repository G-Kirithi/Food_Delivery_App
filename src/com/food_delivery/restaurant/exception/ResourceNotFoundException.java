package com.food_delivery.restaurant.exception;

/**
 * Thrown when an expected restaurant or menu item cannot be found.
 */
public class ResourceNotFoundException extends RestaurantException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

