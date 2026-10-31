package com.food_delivery.payment.model;

import com.food_delivery.payment.enums.DiscountType;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing a promotional discount coupon.
 *
 * <p>Business Rules enforced here:
 * <ul>
 *   <li>A coupon has a unique code, a discount (percentage or flat amount),
 *       a minimum order value and an expiry date.</li>
 *   <li>Only one coupon can be applied per order.</li>
 *   <li>One customer can use the same coupon only once.</li>
 * </ul>
 */
public class Coupon {

    private final String code;
    private final DiscountType discountType;
    private final BigDecimal discountValue;   // flat Rs. amount  OR  percentage (e.g. 10 = 10%)
    private final BigDecimal minimumOrderValue;
    private final LocalDate expiryDate;
    private boolean active;

    /**
     * Constructs a new Coupon.
     *
     * @param code             unique coupon code (case-insensitive stored in UPPER_CASE)
     * @param discountType     FLAT or PERCENTAGE
     * @param discountValue    Rs. amount or percentage value (must be > 0)
     * @param minimumOrderValue minimum cart items total required to apply this coupon
     * @param expiryDate       date after which the coupon cannot be used
     */
    public Coupon(String code,
                  DiscountType discountType,
                  BigDecimal discountValue,
                  BigDecimal minimumOrderValue,
                  LocalDate expiryDate) {
        validateCode(code);
        validateDiscountType(discountType);
        validateDiscountValue(discountValue, discountType);
        validateMinimumOrderValue(minimumOrderValue);
        validateExpiryDate(expiryDate);

        this.code = code.trim().toUpperCase();
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue;
        this.expiryDate = expiryDate;
        this.active = true;
    }

    // --- Getters ---

    public String getCode() {
        return code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks whether this coupon is valid to use on the given date.
     * A coupon is valid if it is active and its expiry date is today or later.
     *
     * @param today reference date (usually LocalDate.now())
     * @return true if valid
     */
    public boolean isValidOn(LocalDate today) {
        return active && !expiryDate.isBefore(today);
    }

    /**
     * Calculates the discount amount to deduct from the given items total.
     * For PERCENTAGE discounts the result is rounded to 2 decimal places.
     * Discount cannot exceed the items total.
     *
     * @param itemsTotal the cart items subtotal (before delivery fee and GST)
     * @return the discount amount in Rs.
     */
    public BigDecimal calculateDiscount(BigDecimal itemsTotal) {
        if (itemsTotal == null || itemsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if (discountType == DiscountType.FLAT) {
            discount = discountValue;
        } else {
            // PERCENTAGE: discountValue is e.g. 10 → 10%
            discount = itemsTotal.multiply(discountValue)
                                 .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
        // Discount cannot exceed the items total
        return discount.min(itemsTotal);
    }

    // --- Validation ---

    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Coupon code cannot be null or blank.");
        }
    }

    private void validateDiscountType(DiscountType type) {
        if (type == null) {
            throw new ValidationException("Coupon discount type cannot be null.");
        }
    }

    private void validateDiscountValue(BigDecimal value, DiscountType type) {
        if (value == null) {
            throw new ValidationException("Coupon discount value cannot be null.");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Coupon discount value must be greater than zero. Provided: " + value);
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("Percentage discount cannot exceed 100%. Provided: " + value);
        }
    }

    private void validateMinimumOrderValue(BigDecimal minimumOrderValue) {
        if (minimumOrderValue == null) {
            throw new ValidationException("Coupon minimum order value cannot be null.");
        }
        if (minimumOrderValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Coupon minimum order value cannot be negative. Provided: " + minimumOrderValue);
        }
    }

    private void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new ValidationException("Coupon expiry date cannot be null.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(code, coupon.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", minimumOrderValue=" + minimumOrderValue +
                ", expiryDate=" + expiryDate +
                ", active=" + active +
                '}';
    }
}
