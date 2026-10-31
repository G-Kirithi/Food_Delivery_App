package com.food_delivery.payment.service;

import com.food_delivery.order.model.Order;
import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.payment.dto.PaymentRequest;
import com.food_delivery.payment.enums.PaymentMethod;
import com.food_delivery.payment.enums.PaymentStatus;
import com.food_delivery.payment.exception.CouponNotFoundException;
import com.food_delivery.payment.exception.CouponValidationException;
import com.food_delivery.payment.exception.PaymentNotFoundException;
import com.food_delivery.payment.exception.PaymentValidationException;
import com.food_delivery.payment.model.Coupon;
import com.food_delivery.payment.model.Payment;
import com.food_delivery.payment.repository.CouponRepository;
import com.food_delivery.payment.repository.PaymentRepository;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link PaymentService} enforcing all Module 5 business rules.
 *
 * <h3>Order Total Formula</h3>
 * <pre>
 *   deliveryFee = Rs. 40  (waived when itemsTotal >= Rs. 500)
 *   GST         = 5 % of itemsTotal
 *   grandTotal  = itemsTotal + deliveryFee + GST - couponDiscount
 * </pre>
 *
 * <h3>COD Limit</h3>
 * <p>COD is allowed only when the order total is Rs. 1500 or less.
 *
 * <h3>Payment Retry Window</h3>
 * <p>If online payment fails, the customer has 15 minutes to retry before the cart is released
 * (Note: cart release on timeout is handled by the calling service/controller layer, this service
 * only validates the retry window).
 */
public class PaymentServiceImpl implements PaymentService {

    // Business-rule constants
    public static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(40);
    public static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);
    public static final BigDecimal GST_RATE = BigDecimal.valueOf(5);        // 5%
    public static final BigDecimal COD_MAX_ORDER_TOTAL = BigDecimal.valueOf(1500);
    public static final long PAYMENT_RETRY_WINDOW_MINUTES = 15;

    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              CouponRepository couponRepository,
                              OrderRepository orderRepository) {
        if (paymentRepository == null) {
            throw new IllegalArgumentException("PaymentRepository cannot be null.");
        }
        if (couponRepository == null) {
            throw new IllegalArgumentException("CouponRepository cannot be null.");
        }
        if (orderRepository == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null.");
        }
        this.paymentRepository = paymentRepository;
        this.couponRepository = couponRepository;
        this.orderRepository = orderRepository;
    }

    // ------------------------------------------------------------------ //
    //  Order Total Calculation                                             //
    // ------------------------------------------------------------------ //

    @Override
    public OrderTotalBreakdown calculateOrderTotal(BigDecimal itemsTotal,
                                                   String couponCode,
                                                   String customerId) {
        if (itemsTotal == null || itemsTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new PaymentValidationException("Items total cannot be null or negative.");
        }

        // 1. Delivery fee (waived when itemsTotal >= Rs. 500)
        BigDecimal deliveryFee = itemsTotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : DELIVERY_FEE;

        // 2. GST = 5% of itemsTotal (rounded to 2 dp)
        BigDecimal gstAmount = itemsTotal
                .multiply(GST_RATE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 3. Coupon discount
        BigDecimal couponDiscount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Coupon coupon = validateAndFetchCoupon(couponCode.trim(), customerId, itemsTotal);
            couponDiscount = coupon.calculateDiscount(itemsTotal);
            appliedCouponCode = coupon.getCode();
        }

        // 4. Grand total
        BigDecimal grandTotal = itemsTotal
                .add(deliveryFee)
                .add(gstAmount)
                .subtract(couponDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        // Total must never be negative
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        return new OrderTotalBreakdown(itemsTotal, deliveryFee, gstAmount,
                couponDiscount, appliedCouponCode, grandTotal);
    }

    // ------------------------------------------------------------------ //
    //  Payment Initiation                                                  //
    // ------------------------------------------------------------------ //

    @Override
    public Payment initiatePayment(PaymentRequest request) {
        validatePaymentRequest(request);

        String orderId = request.getOrderId().trim();
        String customerId = request.getCustomerId().trim();
        PaymentMethod method = request.getPaymentMethod();

        // Retrieve the order to get items total
        Order order = getOrderOrThrow(orderId);

        // Verify customer owns this order
        if (!order.getCustomerId().equals(customerId)) {
            throw new PaymentValidationException(
                    "Customer ID '" + customerId + "' is not authorized to pay for order '" + orderId + "'.");
        }

        // Ensure only one payment per order
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentValidationException(
                    "A payment record already exists for order '" + orderId + "'. Use retryPayment() if the previous payment failed.");
        }

        // Calculate total with optional coupon
        OrderTotalBreakdown breakdown = calculateOrderTotal(
                order.getTotalAmount(),
                request.getCouponCode(),
                customerId
        );

        BigDecimal grandTotal = breakdown.grandTotal;

        // COD limit check: allowed only when total <= Rs. 1500
        if (method == PaymentMethod.COD && grandTotal.compareTo(COD_MAX_ORDER_TOTAL) > 0) {
            throw new PaymentValidationException(
                    "Cash on Delivery (COD) is not allowed for orders exceeding Rs. " + COD_MAX_ORDER_TOTAL +
                    ". Your order total is Rs. " + grandTotal +
                    ". Please use UPI or CARD instead.");
        }

        // Determine initial status:
        // - UPI/CARD → PAID  (simulating successful online gateway)
        // - COD      → PENDING (collected at delivery)
        PaymentStatus initialStatus = method.isOnline() ? PaymentStatus.PAID : PaymentStatus.PENDING;

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                paymentId,
                orderId,
                customerId,
                grandTotal,
                method,
                initialStatus,
                breakdown.appliedCouponCode,
                breakdown.couponDiscount
        );

        paymentRepository.save(payment);

        // Record coupon usage if a coupon was applied
        if (breakdown.appliedCouponCode != null) {
            couponRepository.recordUsage(customerId, breakdown.appliedCouponCode);
        }

        return payment;
    }

    /**
     * Simulates a payment failure for testing purposes.
     * Creates a FAILED payment record; the customer may then retry within 15 minutes.
     *
     * @param request payment request
     * @return FAILED Payment record
     */
    public Payment initiateFailedPayment(PaymentRequest request) {
        validatePaymentRequest(request);

        String orderId = request.getOrderId().trim();
        String customerId = request.getCustomerId().trim();
        PaymentMethod method = request.getPaymentMethod();

        if (!method.isOnline()) {
            throw new PaymentValidationException("A COD payment cannot fail at initiation – it is collected on delivery.");
        }

        Order order = getOrderOrThrow(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new PaymentValidationException(
                    "Customer ID '" + customerId + "' is not authorized to pay for order '" + orderId + "'.");
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentValidationException(
                    "A payment record already exists for order '" + orderId + "'.");
        }

        OrderTotalBreakdown breakdown = calculateOrderTotal(
                order.getTotalAmount(),
                request.getCouponCode(),
                customerId
        );

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Start as PENDING then immediately mark FAILED
        Payment payment = new Payment(
                paymentId,
                orderId,
                customerId,
                breakdown.grandTotal,
                method,
                PaymentStatus.PENDING,   // Start PENDING
                breakdown.appliedCouponCode,
                breakdown.couponDiscount
        );
        payment.markFailed();
        paymentRepository.save(payment);

        return payment;
    }

    @Override
    public Payment retryPayment(String orderId, String customerId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentValidationException("Order ID cannot be null or blank for payment retry.");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("Customer ID cannot be null or blank for payment retry.");
        }

        Payment existingPayment = paymentRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment record found for order '" + orderId + "'. Initiate payment first."));

        // Validate ownership
        if (!existingPayment.getCustomerId().equals(customerId.trim())) {
            throw new PaymentValidationException(
                    "Customer '" + customerId + "' is not authorized to retry payment for order '" + orderId + "'.");
        }

        // Only FAILED payments can be retried
        if (existingPayment.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentValidationException(
                    "Only FAILED payments can be retried. Current payment status: " + existingPayment.getStatus());
        }

        // Enforce 15-minute retry window
        Duration elapsed = Duration.between(existingPayment.getCreatedAt(), Instant.now());
        if (elapsed.toMinutes() >= PAYMENT_RETRY_WINDOW_MINUTES) {
            throw new PaymentValidationException(
                    "Payment retry window of " + PAYMENT_RETRY_WINDOW_MINUTES + " minutes has expired (elapsed: " +
                    elapsed.toMinutes() + " mins). The cart has been released. Please start a new order.");
        }

        // Create a fresh PAID payment record to replace the failed one.
        // Payment.markPaid() only allows transitions from PENDING; since the failed record is in FAILED
        // status, we create a new Payment with PAID as the initial status and overwrite the old record
        // in the repository via the same orderId key in the secondary index.
        String newPaymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment retryPayment = new Payment(
                newPaymentId,
                existingPayment.getOrderId(),
                existingPayment.getCustomerId(),
                existingPayment.getAmount(),
                existingPayment.getPaymentMethod(),
                PaymentStatus.PAID,  // Retry succeeds → start directly as PAID
                existingPayment.getAppliedCouponCode(),
                existingPayment.getCouponDiscount()
        );
        paymentRepository.save(retryPayment);

        // Record coupon usage if a coupon was on the retried payment and not yet tracked
        if (retryPayment.getAppliedCouponCode() != null &&
            !couponRepository.hasCustomerUsedCoupon(customerId.trim(), retryPayment.getAppliedCouponCode())) {
            couponRepository.recordUsage(customerId.trim(), retryPayment.getAppliedCouponCode());
        }

        return retryPayment;
    }

    // ------------------------------------------------------------------ //
    //  Refunds                                                             //
    // ------------------------------------------------------------------ //

    @Override
    public Payment processRefund(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentValidationException("Order ID cannot be null or blank for refund.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment record found for order '" + orderId + "'."));

        // COD payments that are PENDING were never charged — no refund needed
        if (payment.getPaymentMethod() == PaymentMethod.COD &&
            payment.getStatus() == PaymentStatus.PENDING) {
            // Return as-is; no refund action required
            return payment;
        }

        // For PAID online payments → issue full refund
        payment.markRefunded();
        paymentRepository.save(payment);
        return payment;
    }

    // ------------------------------------------------------------------ //
    //  COD Completion                                                      //
    // ------------------------------------------------------------------ //

    @Override
    public Payment markCodPaymentPaid(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentValidationException("Order ID cannot be null or blank.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment record found for order '" + orderId + "'."));

        if (payment.getPaymentMethod() != PaymentMethod.COD) {
            throw new PaymentValidationException(
                    "markCodPaymentPaid() can only be called for COD payments. " +
                    "Payment method for order '" + orderId + "' is: " + payment.getPaymentMethod());
        }

        payment.markPaid();
        paymentRepository.save(payment);
        return payment;
    }

    // ------------------------------------------------------------------ //
    //  Coupon Management                                                   //
    // ------------------------------------------------------------------ //

    @Override
    public Coupon addCoupon(Coupon coupon) {
        if (coupon == null) {
            throw new ValidationException("Coupon cannot be null.");
        }
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new CouponValidationException(
                    "A coupon with code '" + coupon.getCode() + "' already exists.");
        }
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Coupon code cannot be null or blank.");
        }
        return couponRepository.findByCode(code.trim())
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with code: " + code.trim().toUpperCase()));
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    // ------------------------------------------------------------------ //
    //  Payment Lookups                                                     //
    // ------------------------------------------------------------------ //

    @Override
    public Payment getPaymentByPaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new PaymentValidationException("Payment ID cannot be null or blank.");
        }
        return paymentRepository.findByPaymentId(paymentId.trim())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with ID: " + paymentId.trim()));
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentValidationException("Order ID cannot be null or blank.");
        }
        return paymentRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for order ID: " + orderId.trim()));
    }

    @Override
    public List<Payment> getPaymentsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("Customer ID cannot be null or blank.");
        }
        return paymentRepository.findByCustomerId(customerId.trim());
    }

    // ------------------------------------------------------------------ //
    //  Private Helpers                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Validates a coupon for use by a specific customer on a specific items total.
     * Throws CouponNotFoundException or CouponValidationException on failure.
     */
    private Coupon validateAndFetchCoupon(String code, String customerId, BigDecimal itemsTotal) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with code: " + code.toUpperCase()));

        if (!coupon.isValidOn(LocalDate.now())) {
            throw new CouponValidationException(
                    "Coupon '" + code.toUpperCase() + "' has expired or is inactive. Expiry date: " + coupon.getExpiryDate());
        }

        if (itemsTotal.compareTo(coupon.getMinimumOrderValue()) < 0) {
            throw new CouponValidationException(
                    "Coupon '" + code.toUpperCase() + "' requires a minimum order value of Rs. " +
                    coupon.getMinimumOrderValue() + ". Your cart total is Rs. " + itemsTotal + ".");
        }

        if (customerId != null && couponRepository.hasCustomerUsedCoupon(customerId, code)) {
            throw new CouponValidationException(
                    "Coupon '" + code.toUpperCase() + "' has already been used by this customer. " +
                    "Each customer can use the same coupon only once.");
        }

        return coupon;
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new PaymentValidationException("PaymentRequest cannot be null.");
        }
        if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
            throw new PaymentValidationException("Order ID cannot be null or blank in PaymentRequest.");
        }
        if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
            throw new PaymentValidationException("Customer ID cannot be null or blank in PaymentRequest.");
        }
        if (request.getPaymentMethod() == null) {
            throw new PaymentValidationException("Payment method cannot be null in PaymentRequest.");
        }
    }

    private Order getOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentValidationException(
                        "Order not found with ID: " + orderId + ". Cannot initiate payment for a non-existent order."));
    }
}
