package com.food_delivery.payment.exception;

/** Thrown when a coupon fails validation (expired, already used, below minimum order, etc.). */
public class CouponValidationException extends PaymentException {
    public CouponValidationException(String message) {
        super(message);
    }
}
