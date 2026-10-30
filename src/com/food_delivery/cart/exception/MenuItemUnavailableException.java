package com.food_delivery.cart.exception;

/**
 * Thrown when attempting to add an unavailable menu item to the cart.
 */
public class MenuItemUnavailableException extends CartException {
    public MenuItemUnavailableException(String message) {
        super(message);
    }
}
