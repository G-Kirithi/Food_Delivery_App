package com.food_delivery.payment.repository;

import com.food_delivery.payment.model.Coupon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link CouponRepository}.
 * Stores coupons keyed by their UPPER_CASE code and tracks per-customer usage.
 */
public class InMemoryCouponRepository implements CouponRepository {

    // coupon code (uppercase) → Coupon
    private final Map<String, Coupon> couponStore = new ConcurrentHashMap<>();

    // "customerId::couponCode" composite key set for O(1) usage checks
    private final Set<String> usageSet = ConcurrentHashMap.newKeySet();

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null.");
        }
        couponStore.put(coupon.getCode(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(couponStore.get(code.trim().toUpperCase()));
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(couponStore.values());
    }

    @Override
    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return couponStore.containsKey(code.trim().toUpperCase());
    }

    @Override
    public void recordUsage(String customerId, String couponCode) {
        if (customerId == null || couponCode == null) {
            return;
        }
        usageSet.add(customerId.trim() + "::" + couponCode.trim().toUpperCase());
    }

    @Override
    public boolean hasCustomerUsedCoupon(String customerId, String couponCode) {
        if (customerId == null || couponCode == null) {
            return false;
        }
        return usageSet.contains(customerId.trim() + "::" + couponCode.trim().toUpperCase());
    }
}
