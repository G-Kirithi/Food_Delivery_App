package com.food_delivery.payment.enums;

/**
 * Type of discount applied by a coupon.
 */
public enum DiscountType {
    /** A fixed rupee amount is deducted from the order total. */
    FLAT,
    /** A percentage of the order total is deducted. */
    PERCENTAGE
}
