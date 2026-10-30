package com.food_delivery.order.model;

import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.exception.InvalidOrderStateException;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing an Order in the Food Delivery platform.
 * Encapsulates the entire lifecycle, frozen item pricing, address snapshot,
 * timestamps, and strict unidirectional status transitions.
 */
public class Order {
    private final String id;
    private final String customerId;
    private final String customerName;
    private final String restaurantId;
    private final String restaurantName;
    private final String deliveryAddressId;
    private final String deliveryCity;
    private final String deliveryFullAddress;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;

    private OrderStatus status;
    private String rejectionReason;
    private String cancellationReason;
    private String deliveryPartnerId;

    // Lifecycle timestamps
    private final Instant placedAt;
    private Instant acceptedAt;
    private Instant preparingAt;
    private Instant readyForPickupAt;
    private Instant outForDeliveryAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private Instant rejectedAt;

    public Order(String id,
                 String customerId,
                 String customerName,
                 String restaurantId,
                 String restaurantName,
                 String deliveryAddressId,
                 String deliveryCity,
                 String deliveryFullAddress,
                 List<OrderItem> items,
                 BigDecimal totalAmount,
                 Instant placedAt) {
        validateId(id);
        validateCustomerId(customerId);
        validateRestaurantId(restaurantId);
        validateDeliveryCity(deliveryCity);
        validateItems(items);
        validateTotalAmount(totalAmount);

        this.id = id.trim();
        this.customerId = customerId.trim();
        this.customerName = (customerName != null) ? customerName.trim() : "";
        this.restaurantId = restaurantId.trim();
        this.restaurantName = (restaurantName != null) ? restaurantName.trim() : "";
        this.deliveryAddressId = (deliveryAddressId != null) ? deliveryAddressId.trim() : "";
        this.deliveryCity = deliveryCity.trim();
        this.deliveryFullAddress = (deliveryFullAddress != null) ? deliveryFullAddress.trim() : "";
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.totalAmount = totalAmount;
        this.placedAt = (placedAt != null) ? placedAt : Instant.now();
        this.status = OrderStatus.PLACED;
    }

    public Order(String id,
                 String customerId,
                 String customerName,
                 String restaurantId,
                 String restaurantName,
                 String deliveryAddressId,
                 String deliveryCity,
                 String deliveryFullAddress,
                 List<OrderItem> items,
                 BigDecimal totalAmount) {
        this(id, customerId, customerName, restaurantId, restaurantName,
             deliveryAddressId, deliveryCity, deliveryFullAddress, items, totalAmount, Instant.now());
    }

    // --- Getters ---

    public String getId() {
        return id;
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

    public String getDeliveryAddressId() {
        return deliveryAddressId;
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

    public String getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(String deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
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

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public boolean isActive() {
        return status.isActive();
    }

    // --- State Transition Methods ---

    /**
     * Advances the order to the given next status if permitted by the lifecycle rules.
     *
     * @param nextStatus target status
     */
    public synchronized void transitionTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new ValidationException("Target order status cannot be null.");
        }
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidOrderStateException(
                    "Illegal order status transition from '" + this.status + "' to '" + nextStatus +
                    "'. Orders must follow strict sequential order and cannot skip steps or move backwards."
            );
        }

        this.status = nextStatus;
        Instant now = Instant.now();

        switch (nextStatus) {
            case ACCEPTED -> this.acceptedAt = now;
            case PREPARING -> this.preparingAt = now;
            case READY_FOR_PICKUP -> this.readyForPickupAt = now;
            case OUT_FOR_DELIVERY -> this.outForDeliveryAt = now;
            case DELIVERED -> this.deliveredAt = now;
            default -> {}
        }
    }

    /**
     * Rejects the order from PLACED status. Rejection is only allowed in PLACED status
     * and requires a mandatory reason.
     *
     * @param reason non-blank reason for rejection
     */
    public synchronized void reject(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Rejection reason is mandatory when rejecting an order.");
        }
        if (this.status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException(
                    "A restaurant can reject an order only while it is in PLACED status. Current status: " + this.status
            );
        }

        this.status = OrderStatus.REJECTED;
        this.rejectionReason = reason.trim();
        this.rejectedAt = Instant.now();
    }

    /**
     * Cancels the order by the customer. Cancellation is only allowed in PLACED or ACCEPTED status.
     * Once preparation starts, cancellation is strictly rejected.
     *
     * @param reason reason for customer cancellation
     */
    public synchronized void cancelByCustomer(String reason) {
        if (this.status != OrderStatus.PLACED && this.status != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException(
                    "A customer can cancel an order only while it is in PLACED or ACCEPTED status. " +
                    "Cancellation is not allowed once preparation starts (Current status: " + this.status + ")."
            );
        }

        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Cancelled by customer";
        this.cancelledAt = Instant.now();
    }

    /**
     * Automatically cancels the order due to restaurant non-acceptance within the allowed window.
     *
     * @param reason timeout cancellation message
     */
    public synchronized void autoCancel(String reason) {
        if (this.status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException("Auto-cancellation can only be triggered for orders in PLACED status.");
        }

        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Auto-cancelled: Restaurant did not accept within 5 minutes";
        this.cancelledAt = Instant.now();
    }

    // --- Validation Helpers ---

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Order ID cannot be null or blank.");
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank.");
        }
    }

    private void validateRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
    }

    private void validateDeliveryCity(String deliveryCity) {
        if (deliveryCity == null || deliveryCity.trim().isEmpty()) {
            throw new ValidationException("Delivery city cannot be null or blank.");
        }
    }

    private void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Order must contain at least one item.");
        }
    }

    private void validateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new ValidationException("Total amount cannot be null.");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Total amount must be greater than zero. Provided: " + totalAmount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", deliveryCity='" + deliveryCity + '\'' +
                ", itemCount=" + items.size() +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", placedAt=" + placedAt +
                '}';
    }
}
