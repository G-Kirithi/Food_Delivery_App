package com.food_delivery.customer.exception;

/**
 * Thrown when attempting to register a customer with a phone number that is already in use.
 */
public class DuplicatePhoneNumberException extends CustomerException {
    public DuplicatePhoneNumberException(String message) {
        super(message);
    }
}
