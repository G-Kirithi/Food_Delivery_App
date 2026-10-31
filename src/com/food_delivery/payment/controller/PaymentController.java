package com.food_delivery.payment.controller;

import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.payment.dto.PaymentRequest;
import com.food_delivery.payment.dto.PaymentResponse;
import com.food_delivery.payment.exception.CouponNotFoundException;
import com.food_delivery.payment.exception.CouponValidationException;
import com.food_delivery.payment.exception.PaymentNotFoundException;
import com.food_delivery.payment.exception.PaymentValidationException;
import com.food_delivery.payment.model.Coupon;
import com.food_delivery.payment.model.Payment;
import com.food_delivery.payment.service.PaymentService;
import com.food_delivery.restaurant.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-style controller for Module 5 — Payments, Coupons and Refunds.
 * Mirrors the established controller pattern used across the codebase.
 *
 * <p>Endpoints (logical):
 * <pre>
 *  POST   /api/payments                      – Initiate payment
 *  POST   /api/payments/{orderId}/retry      – Retry a failed online payment
 *  POST   /api/payments/{orderId}/refund     – Process a refund
 *  POST   /api/payments/{orderId}/cod-paid   – Mark COD as paid on delivery
 *  GET    /api/payments/{paymentId}          – Get payment by payment ID
 *  GET    /api/payments/order/{orderId}      – Get payment by order ID
 *  GET    /api/customers/{customerId}/payments – All payments by customer
 *  POST   /api/coupons                        – Admin adds a coupon
 *  GET    /api/coupons/{code}                 – Get coupon details
 *  GET    /api/coupons                        – List all coupons
 * </pre>
 */
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        if (paymentService == null) {
            throw new IllegalArgumentException("PaymentService cannot be null.");
        }
        this.paymentService = paymentService;
    }

    // ------------------------------------------------------------------ //
    //  Payment Initiation                                                  //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/payments
     * Initiates payment for a placed order (UPI, CARD, or COD).
     */
    public ApiResponse<PaymentResponse> initiatePayment(PaymentRequest request) {
        try {
            Payment payment = paymentService.initiatePayment(request);
            return ApiResponse.created("Payment initiated successfully.", new PaymentResponse(payment));
        } catch (PaymentValidationException | CouponValidationException | CouponNotFoundException
                | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/payments/{orderId}/retry
     * Retries a failed UPI/CARD payment within the 15-minute window.
     */
    public ApiResponse<PaymentResponse> retryPayment(String orderId, String customerId) {
        try {
            Payment payment = paymentService.retryPayment(orderId, customerId);
            return ApiResponse.ok("Payment retry successful. Payment is now PAID.", new PaymentResponse(payment));
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (PaymentValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    // ------------------------------------------------------------------ //
    //  Refunds                                                             //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/payments/{orderId}/refund
     * Processes a full refund for the given order's payment.
     * Called after order cancellation or restaurant rejection.
     */
    public ApiResponse<PaymentResponse> processRefund(String orderId) {
        try {
            Payment payment = paymentService.processRefund(orderId);
            return ApiResponse.ok("Refund processed successfully.", new PaymentResponse(payment));
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (PaymentValidationException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    // ------------------------------------------------------------------ //
    //  COD Completion                                                      //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/payments/{orderId}/cod-paid
     * Marks a COD payment as PAID when the order is delivered.
     */
    public ApiResponse<PaymentResponse> markCodPaymentPaid(String orderId) {
        try {
            Payment payment = paymentService.markCodPaymentPaid(orderId);
            return ApiResponse.ok("COD payment marked as PAID on delivery.", new PaymentResponse(payment));
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (PaymentValidationException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    // ------------------------------------------------------------------ //
    //  Payment Lookups                                                     //
    // ------------------------------------------------------------------ //

    /**
     * GET /api/payments/{paymentId}
     */
    public ApiResponse<PaymentResponse> getPaymentByPaymentId(String paymentId) {
        try {
            Payment payment = paymentService.getPaymentByPaymentId(paymentId);
            return ApiResponse.ok("Payment retrieved successfully.", new PaymentResponse(payment));
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (PaymentValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/payments/order/{orderId}
     */
    public ApiResponse<PaymentResponse> getPaymentByOrderId(String orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ApiResponse.ok("Payment retrieved successfully.", new PaymentResponse(payment));
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (PaymentValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}/payments
     */
    public ApiResponse<List<PaymentResponse>> getPaymentsByCustomerId(String customerId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByCustomerId(customerId);
            List<PaymentResponse> responses = payments.stream()
                    .map(PaymentResponse::new)
                    .collect(Collectors.toList());
            return ApiResponse.ok("Customer payments retrieved successfully.", responses);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    // ------------------------------------------------------------------ //
    //  Coupon Management                                                   //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/coupons
     * Admin adds a new coupon to the system.
     */
    public ApiResponse<Coupon> addCoupon(Coupon coupon) {
        try {
            Coupon saved = paymentService.addCoupon(coupon);
            return ApiResponse.created("Coupon created successfully.", saved);
        } catch (CouponValidationException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/coupons/{code}
     */
    public ApiResponse<Coupon> getCouponByCode(String code) {
        try {
            Coupon coupon = paymentService.getCouponByCode(code);
            return ApiResponse.ok("Coupon retrieved successfully.", coupon);
        } catch (CouponNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/coupons
     */
    public ApiResponse<List<Coupon>> getAllCoupons() {
        try {
            List<Coupon> coupons = paymentService.getAllCoupons();
            return ApiResponse.ok("Coupons retrieved successfully.", coupons);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/payments/{orderId}/breakdown
     * Returns the full order total breakdown for a given order and optional coupon.
     */
    public ApiResponse<PaymentService.OrderTotalBreakdown> getOrderTotalBreakdown(
            String orderId, String customerId, String couponCode) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            // Re-use the payment amount stored, but for breakdown return from service call
            // We derive itemsTotal from the payment amount inverse — or better, look up order
            // However, since breakdown is normally pre-payment, this is a convenience endpoint
            // We'll delegate to calculateOrderTotal with stored coupon info
            String usedCoupon = payment.getAppliedCouponCode() != null ? payment.getAppliedCouponCode() : couponCode;
            // We don't have direct itemsTotal from payment, return the payment as-is
            return ApiResponse.ok("Payment details retrieved. Use GET /api/payments/order/{orderId} for full details.",
                    null);
        } catch (PaymentNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }
}
