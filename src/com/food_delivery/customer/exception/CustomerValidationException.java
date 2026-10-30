package com.food_delivery.customer.exception;

/**
 * Thrown when customer input validation fails (e.g. empty name, invalid email, invalid phone format).
 */
public class CustomerValidationException extends CustomerException {
    public CustomerValidationException(String message) {
        super(message);
    }
}
