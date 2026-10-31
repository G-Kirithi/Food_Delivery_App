package com.food_delivery.payment.dto;

import com.food_delivery.payment.enums.PaymentMethod;

/**
 * Request DTO for initiating a payment for an order.
 *
 * <p>The client provides:
 * <ul>
 *   <li>orderId        – the order being paid for</li>
 *   <li>customerId     – the paying customer</li>
 *   <li>paymentMethod  – UPI, CARD, or COD</li>
 *   <li>couponCode     – optional promotional code (null / blank = no coupon)</li>
 * </ul>
 */
public class PaymentRequest {

    private final String orderId;
    private final String customerId;
    private final PaymentMethod paymentMethod;
    private final String couponCode; // nullable

    public PaymentRequest(String orderId, String customerId, PaymentMethod paymentMethod, String couponCode) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.couponCode = couponCode;
    }

    /** Convenience constructor when no coupon is used. */
    public PaymentRequest(String orderId, String customerId, PaymentMethod paymentMethod) {
        this(orderId, customerId, paymentMethod, null);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getCouponCode() {
        return couponCode;
    }
}
