package com.food_delivery.payment.repository;

import com.food_delivery.payment.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Payment persistence (in-memory / DB agnostic).
 */
public interface PaymentRepository {

    /** Saves (inserts or updates) a payment record. Returns the saved payment. */
    Payment save(Payment payment);

    /** Finds a payment by its unique payment ID. */
    Optional<Payment> findByPaymentId(String paymentId);

    /** Finds the payment record for a given order ID (one payment per order). */
    Optional<Payment> findByOrderId(String orderId);

    /** Returns all payments made by a specific customer. */
    List<Payment> findByCustomerId(String customerId);

    /** Returns all payment records in the system. */
    List<Payment> findAll();

    /** Checks whether a payment exists for the given order. */
    boolean existsByOrderId(String orderId);
}
