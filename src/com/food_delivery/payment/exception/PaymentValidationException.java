package com.food_delivery.payment.exception;

/** Thrown when a payment operation violates business rules (e.g. COD limit, online pre-pay). */
public class PaymentValidationException extends PaymentException {
    public PaymentValidationException(String message) {
        super(message);
    }
}
