package com.food_delivery.customer.exception;

/**
 * Thrown when an address violates business constraints (e.g. invalid fields, deleting last address).
 */
public class InvalidAddressException extends CustomerException {
    public InvalidAddressException(String message) {
        super(message);
    }
}
