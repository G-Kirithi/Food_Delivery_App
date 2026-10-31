package com.food_delivery.payment.enums;

/**
 * Supported payment methods in the Food Delivery platform.
 * <ul>
 *   <li>UPI  – Unified Payment Interface (online; must be completed before restaurant receives order)</li>
 *   <li>CARD – Credit / Debit Card (online; same pre-payment rule as UPI)</li>
 *   <li>COD  – Cash on Delivery (offline; allowed only when order total is Rs. 1500 or less)</li>
 * </ul>
 */
public enum PaymentMethod {
    UPI,
    CARD,
    COD;

    /** Returns true for payment methods that must be completed before order placement. */
    public boolean isOnline() {
        return this == UPI || this == CARD;
    }
}
