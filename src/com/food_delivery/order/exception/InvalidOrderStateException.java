package com.food_delivery.order.exception;

/**
 * Thrown when an order state transition is illegal or out of sequence
 * (e.g., trying to cancel an order that is already in PREPARING,
 * trying to skip states, or moving backwards).
 */
public class InvalidOrderStateException extends OrderException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
