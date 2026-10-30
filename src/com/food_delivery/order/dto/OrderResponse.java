package com.food_delivery.order.dto;

import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object representing order details for presentation and API responses.
 */
public class OrderResponse {
    private final String orderId;
    private final String customerId;
    private final String customerName;
    private final String restaurantId;
    private final String restaurantName;
    private final String deliveryCity;
    private final String deliveryFullAddress;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    private final String rejectionReason;
    private final String cancellationReason;
    private final Instant placedAt;
    private final Instant acceptedAt;
    private final Instant preparingAt;
    private final Instant readyForPickupAt;
    private final Instant outForDeliveryAt;
    private final Instant deliveredAt;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.customerId = order.getCustomerId();
        this.customerName = order.getCustomerName();
        this.restaurantId = order.getRestaurantId();
        this.restaurantName = order.getRestaurantName();
        this.deliveryCity = order.getDeliveryCity();
        this.deliveryFullAddress = order.getDeliveryFullAddress();
        this.items = order.getItems();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.rejectionReason = order.getRejectionReason();
        this.cancellationReason = order.getCancellationReason();
        this.placedAt = order.getPlacedAt();
        this.acceptedAt = order.getAcceptedAt();
        this.preparingAt = order.getPreparingAt();
        this.readyForPickupAt = order.getReadyForPickupAt();
        this.outForDeliveryAt = order.getOutForDeliveryAt();
        this.deliveredAt = order.getDeliveredAt();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public String getDeliveryFullAddress() {
        return deliveryFullAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getPreparingAt() {
        return preparingAt;
    }

    public Instant getReadyForPickupAt() {
        return readyForPickupAt;
    }

    public Instant getOutForDeliveryAt() {
        return outForDeliveryAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}
