package com.food_delivery.payment.exception;

/** Thrown when a coupon code is not found in the repository. */
public class CouponNotFoundException extends PaymentException {
    public CouponNotFoundException(String message) {
        super(message);
    }
}
