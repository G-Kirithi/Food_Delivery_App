package com.food_delivery.payment.enums;

/**
 * Lifecycle status of a Payment record.
 * <ul>
 *   <li>PENDING  – COD orders start here; also used while awaiting UPI/Card completion.</li>
 *   <li>PAID     – Payment successfully captured (or COD order delivered).</li>
 *   <li>FAILED   – Payment attempt was unsuccessful.</li>
 *   <li>REFUNDED – A refund has been issued back to the customer.</li>
 * </ul>
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
