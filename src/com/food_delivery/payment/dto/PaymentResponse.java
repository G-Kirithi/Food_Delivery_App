package com.food_delivery.payment.dto;

import com.food_delivery.payment.enums.PaymentMethod;
import com.food_delivery.payment.enums.PaymentStatus;
import com.food_delivery.payment.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only response DTO for Payment data returned by the API layer.
 * Converts a {@link Payment} domain entity into a serializable structure.
 */
public class PaymentResponse {

    private final String paymentId;
    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final BigDecimal couponDiscount;
    private final String appliedCouponCode;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus status;
    private final Instant createdAt;
    private final Instant paidAt;
    private final Instant refundedAt;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrderId();
        this.customerId = payment.getCustomerId();
        this.amount = payment.getAmount();
        this.couponDiscount = payment.getCouponDiscount();
        this.appliedCouponCode = payment.getAppliedCouponCode();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.createdAt = payment.getCreatedAt();
        this.paidAt = payment.getPaidAt();
        this.refundedAt = payment.getRefundedAt();
    }

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getCouponDiscount() { return couponDiscount; }
    public String getAppliedCouponCode() { return appliedCouponCode; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getRefundedAt() { return refundedAt; }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", couponDiscount=" + couponDiscount +
                ", method=" + paymentMethod +
                ", status=" + status +
                '}';
    }
}
