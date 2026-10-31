package com.food_delivery.payment.exception;

/** Thrown when a payment record cannot be found in the repository. */
public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
