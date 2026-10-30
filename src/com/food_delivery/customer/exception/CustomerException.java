package com.food_delivery.customer.exception;

/**
 * Base unchecked exception for customer domain errors.
 */
public class CustomerException extends RuntimeException {
    public CustomerException(String message) {
        super(message);
    }

    public CustomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
