package com.food_delivery.restaurant.exception;

/**
 * Thrown when attempting to create a restaurant or menu item with an already existing ID.
 */
public class DuplicateResourceException extends RestaurantException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

