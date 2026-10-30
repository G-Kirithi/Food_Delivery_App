package com.food_delivery.restaurant.exception;

/**
 * Thrown when an unauthorized user role attempts to perform an administrative action.
 */
public class UnauthorizedException extends RestaurantException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

