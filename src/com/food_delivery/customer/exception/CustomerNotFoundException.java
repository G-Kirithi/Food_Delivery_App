package com.food_delivery.customer.exception;

/**
 * Thrown when a customer is not found with the requested identifier.
 */
public class CustomerNotFoundException extends CustomerException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
