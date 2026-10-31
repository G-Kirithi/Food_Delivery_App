package com.food_delivery.payment.repository;

import com.food_delivery.payment.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of {@link PaymentRepository}.
 * Uses a ConcurrentHashMap keyed by payment ID, consistent with the rest of the codebase.
 */
public class InMemoryPaymentRepository implements PaymentRepository {

    // Primary store: paymentId → Payment
    private final Map<String, Payment> paymentStore = new ConcurrentHashMap<>();

    // Secondary index: orderId → paymentId  (one payment per order)
    private final Map<String, String> orderIndex = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }
        paymentStore.put(payment.getPaymentId(), payment);
        orderIndex.put(payment.getOrderId(), payment.getPaymentId());
        return payment;
    }

    @Override
    public Optional<Payment> findByPaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(paymentStore.get(paymentId.trim()));
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Optional.empty();
        }
        String pid = orderIndex.get(orderId.trim());
        if (pid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(paymentStore.get(pid));
    }

    @Override
    public List<Payment> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String clean = customerId.trim();
        return paymentStore.values().stream()
                .filter(p -> clean.equals(p.getCustomerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(paymentStore.values());
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        return orderIndex.containsKey(orderId.trim());
    }
}
