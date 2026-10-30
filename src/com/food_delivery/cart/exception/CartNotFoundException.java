package com.food_delivery.cart.exception;

/**
 * Thrown when the cart for a customer or cart ID cannot be found.
 */
public class CartNotFoundException extends CartException {
    public CartNotFoundException(String message) {
        super(message);
    }
}
