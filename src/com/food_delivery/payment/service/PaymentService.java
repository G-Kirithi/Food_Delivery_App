package com.food_delivery.payment.service;

import com.food_delivery.payment.dto.PaymentRequest;
import com.food_delivery.payment.model.Coupon;
import com.food_delivery.payment.model.Payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for Module 5 — Payments, Coupons and Refunds.
 *
 * <p>Enforced business rules:
 * <ol>
 *   <li>Payment methods: UPI, CARD (online – must complete before order reaches restaurant),
 *       COD (offline – allowed only when order total ≤ Rs. 1500).</li>
 *   <li>Order total = items total + delivery fee (Rs. 40, free ≥ Rs. 500) + 5% GST – coupon discount.</li>
 *   <li>If UPI/Card payment fails the order is NOT placed and customer has 15 minutes to retry.</li>
 *   <li>Only one coupon per order; each customer may use the same coupon only once.</li>
 *   <li>Full refund on: customer cancels within allowed window, restaurant rejection, auto-cancel.</li>
 *   <li>COD stays PENDING until delivery, then becomes PAID.</li>
 * </ol>
 */
public interface PaymentService {

    // ------------------------------------------------------------------ //
    //  Order Total Calculation                                             //
    // ------------------------------------------------------------------ //

    /**
     * Calculates the full order total according to Module 5 formula:
     * <pre>
     *   delivery fee = Rs. 40  (waived when itemsTotal >= Rs. 500)
     *   GST          = 5% of itemsTotal
     *   total        = itemsTotal + deliveryFee + GST - couponDiscount
     * </pre>
     *
     * @param itemsTotal    raw cart items total (sum of price × qty)
     * @param couponCode    optional coupon code (null / blank = no coupon)
     * @param customerId    customer ID needed to validate coupon eligibility
     * @return breakdown result containing total and line items
     */
    OrderTotalBreakdown calculateOrderTotal(BigDecimal itemsTotal, String couponCode, String customerId);

    // ------------------------------------------------------------------ //
    //  Payment Initiation                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Initiates payment for an already-placed order.
     *
     * <p>For UPI / Card:
     * <ul>
     *   <li>Simulates online payment – creates a PAID payment record immediately.</li>
     *   <li>If it fails, creates a FAILED record; order must not proceed to restaurant.</li>
     * </ul>
     *
     * <p>For COD:
     * <ul>
     *   <li>Allowed only when order total ≤ Rs. 1500.</li>
     *   <li>Creates a PENDING payment record.</li>
     * </ul>
     *
     * @param request payment request containing orderId, customerId, method, optional coupon
     * @return the created Payment record
     */
    Payment initiatePayment(PaymentRequest request);

    /**
     * Retries a failed online (UPI/CARD) payment within the 15-minute retry window.
     *
     * @param orderId    the order to retry payment for
     * @param customerId customer ID (authorization check)
     * @return the updated Payment record (PAID on success)
     */
    Payment retryPayment(String orderId, String customerId);

    // ------------------------------------------------------------------ //
    //  Refunds                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Issues a full refund for a paid online payment when:
     * <ul>
     *   <li>Customer cancels in PLACED or ACCEPTED status (allowed window).</li>
     *   <li>Restaurant rejects the order.</li>
     *   <li>Order is auto-cancelled due to acceptance timeout.</li>
     * </ul>
     * COD orders that are PENDING (never charged) are skipped – no refund needed.
     *
     * @param orderId the order whose payment must be refunded
     * @return the updated Payment record in REFUNDED status, or the unchanged PENDING record for COD
     */
    Payment processRefund(String orderId);

    // ------------------------------------------------------------------ //
    //  COD Completion                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Marks the COD payment as PAID when the order is delivered to the customer.
     * Only applicable for COD orders in PENDING status.
     *
     * @param orderId the delivered order
     * @return the updated Payment record in PAID status
     */
    Payment markCodPaymentPaid(String orderId);

    // ------------------------------------------------------------------ //
    //  Coupon Management                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Adds a new coupon to the system. Only admins should call this.
     *
     * @param coupon coupon to add
     * @return saved Coupon
     */
    Coupon addCoupon(Coupon coupon);

    /**
     * Retrieves a coupon by code (case-insensitive).
     *
     * @param code coupon code
     * @return the Coupon entity
     */
    Coupon getCouponByCode(String code);

    /**
     * Returns all registered coupons.
     *
     * @return list of all coupons
     */
    List<Coupon> getAllCoupons();

    // ------------------------------------------------------------------ //
    //  Payment Lookups                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Retrieves a payment record by its payment ID.
     *
     * @param paymentId unique payment ID
     * @return Payment entity
     */
    Payment getPaymentByPaymentId(String paymentId);

    /**
     * Retrieves the payment record for a specific order.
     *
     * @param orderId order ID
     * @return Payment entity
     */
    Payment getPaymentByOrderId(String orderId);

    /**
     * Retrieves all payment records for a specific customer.
     *
     * @param customerId customer ID
     * @return list of payments
     */
    List<Payment> getPaymentsByCustomerId(String customerId);

    // ------------------------------------------------------------------ //
    //  Inner DTO for total breakdown                                       //
    // ------------------------------------------------------------------ //

    /**
     * Immutable value object holding the line-by-line order total breakdown.
     */
    final class OrderTotalBreakdown {
        public final BigDecimal itemsTotal;
        public final BigDecimal deliveryFee;
        public final BigDecimal gstAmount;
        public final BigDecimal couponDiscount;
        public final String appliedCouponCode;
        public final BigDecimal grandTotal;

        public OrderTotalBreakdown(BigDecimal itemsTotal,
                                   BigDecimal deliveryFee,
                                   BigDecimal gstAmount,
                                   BigDecimal couponDiscount,
                                   String appliedCouponCode,
                                   BigDecimal grandTotal) {
            this.itemsTotal = itemsTotal;
            this.deliveryFee = deliveryFee;
            this.gstAmount = gstAmount;
            this.couponDiscount = couponDiscount;
            this.appliedCouponCode = appliedCouponCode;
            this.grandTotal = grandTotal;
        }

        @Override
        public String toString() {
            return "OrderTotalBreakdown{" +
                    "itemsTotal=" + itemsTotal +
                    ", deliveryFee=" + deliveryFee +
                    ", gstAmount=" + gstAmount +
                    ", couponDiscount=" + couponDiscount +
                    ", appliedCouponCode='" + appliedCouponCode + '\'' +
                    ", grandTotal=" + grandTotal +
                    '}';
        }
    }
}
