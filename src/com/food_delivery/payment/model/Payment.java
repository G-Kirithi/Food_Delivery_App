package com.food_delivery.payment.model;

import com.food_delivery.payment.enums.PaymentMethod;
import com.food_delivery.payment.enums.PaymentStatus;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a Payment record for a single order.
 *
 * <p>Business Rules captured here:
 * <ul>
 *   <li>Stores the amount, date, payment method and status.</li>
 *   <li>COD payments start as PENDING and become PAID only when the order is Delivered.</li>
 *   <li>UPI / Card payments must transition to PAID before the restaurant receives the order.</li>
 *   <li>A payment can be refunded (REFUNDED status) on order cancellation / rejection.</li>
 * </ul>
 */
public class Payment {

    private final String paymentId;
    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final Instant createdAt;

    private PaymentStatus status;
    private Instant paidAt;
    private Instant refundedAt;

    // Optional – coupon code that was applied
    private final String appliedCouponCode;
    private final BigDecimal couponDiscount;

    /**
     * Full constructor used by the payment service when creating a new payment record.
     *
     * @param paymentId        unique payment ID
     * @param orderId          linked order ID
     * @param customerId       paying customer ID
     * @param amount           total amount charged (includes delivery fee and GST, minus coupon discount)
     * @param paymentMethod    UPI, CARD, or COD
     * @param initialStatus    starting status (PAID for online methods, PENDING for COD)
     * @param appliedCouponCode coupon code used (may be null)
     * @param couponDiscount   discount amount applied (zero if no coupon)
     */
    public Payment(String paymentId,
                   String orderId,
                   String customerId,
                   BigDecimal amount,
                   PaymentMethod paymentMethod,
                   PaymentStatus initialStatus,
                   String appliedCouponCode,
                   BigDecimal couponDiscount) {
        validatePaymentId(paymentId);
        validateOrderId(orderId);
        validateCustomerId(customerId);
        validateAmount(amount);
        validatePaymentMethod(paymentMethod);
        validateInitialStatus(initialStatus);

        this.paymentId = paymentId.trim();
        this.orderId = orderId.trim();
        this.customerId = customerId.trim();
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = initialStatus;
        this.createdAt = Instant.now();
        this.appliedCouponCode = (appliedCouponCode != null && !appliedCouponCode.trim().isEmpty())
                ? appliedCouponCode.trim().toUpperCase() : null;
        this.couponDiscount = (couponDiscount != null) ? couponDiscount : BigDecimal.ZERO;

        // Mark paidAt immediately for online methods that start in PAID status
        if (initialStatus == PaymentStatus.PAID) {
            this.paidAt = this.createdAt;
        }
    }

    // --- Getters ---

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    // --- State Transitions ---

    /**
     * Marks this payment as PAID (used for COD when order is delivered).
     * Only valid from PENDING status.
     *
     * @throws ValidationException if the payment is not in PENDING status
     */
    public synchronized void markPaid() {
        if (this.status != PaymentStatus.PENDING) {
            throw new ValidationException(
                "Payment can only be marked PAID from PENDING status. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = Instant.now();
    }

    /**
     * Marks this payment as FAILED (used when online payment gateway returns failure).
     * Only valid from PENDING status.
     *
     * @throws ValidationException if the payment is not in PENDING status
     */
    public synchronized void markFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new ValidationException(
                "Payment can only be marked FAILED from PENDING status. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.FAILED;
    }

    /**
     * Marks this payment as REFUNDED.
     * A refund is only applicable for payments that are in PAID status.
     * COD payments (PENDING) are never charged, so they don't need a refund.
     *
     * @throws ValidationException if the payment is not in PAID status
     */
    public synchronized void markRefunded() {
        if (this.status != PaymentStatus.PAID) {
            throw new ValidationException(
                "Only PAID payments can be refunded. Current status: " + this.status +
                ". COD orders that are PENDING were never charged and do not need a refund."
            );
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }

    // --- Validation Helpers ---

    private void validatePaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new ValidationException("Payment ID cannot be null or blank.");
        }
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException("Order ID cannot be null or blank in payment record.");
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank in payment record.");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Payment amount cannot be null.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be greater than zero. Provided: " + amount);
        }
    }

    private void validatePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new ValidationException("Payment method cannot be null.");
        }
    }

    private void validateInitialStatus(PaymentStatus status) {
        if (status == null) {
            throw new ValidationException("Initial payment status cannot be null.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", method=" + paymentMethod +
                ", status=" + status +
                ", couponDiscount=" + couponDiscount +
                ", createdAt=" + createdAt +
                '}';
    }
}
