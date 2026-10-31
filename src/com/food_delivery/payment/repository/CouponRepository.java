package com.food_delivery.payment.repository;

import com.food_delivery.payment.model.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Coupon persistence.
 */
public interface CouponRepository {

    /** Saves (inserts or updates) a coupon. Returns the saved coupon. */
    Coupon save(Coupon coupon);

    /** Finds a coupon by its unique code (case-insensitive lookup). */
    Optional<Coupon> findByCode(String code);

    /** Returns all coupons in the system. */
    List<Coupon> findAll();

    /** Checks whether a coupon with the given code exists. */
    boolean existsByCode(String code);

    /**
     * Records that a specific customer has used this coupon code.
     * Used to enforce the "one customer can use the same coupon only once" rule.
     *
     * @param customerId customer ID
     * @param couponCode coupon code
     */
    void recordUsage(String customerId, String couponCode);

    /**
     * Returns true if the given customer has already used this coupon code.
     *
     * @param customerId customer ID
     * @param couponCode coupon code
     * @return true if already used
     */
    boolean hasCustomerUsedCoupon(String customerId, String couponCode);
}
