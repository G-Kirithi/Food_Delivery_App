package com.food_delivery.order.exception;

/**
 * Thrown when an order operation times out (e.g. restaurant failing to accept within 5 minutes).
 */
public class OrderTimeoutException extends OrderException {
    public OrderTimeoutException(String message) {
        super(message);
    }
}
