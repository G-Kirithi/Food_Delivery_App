package com.food_delivery.order.enums;

/**
 * Enumeration of all possible order statuses in the food delivery platform.
 * Enforces the strict sequential lifecycle:
 * PLACED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED
 * with terminal cancellation and rejection paths.
 */
public enum OrderStatus {
    PLACED,
    ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REJECTED;

    /**
     * An active order is defined as any order that is NOT in a terminal state
     * (DELIVERED, CANCELLED, or REJECTED).
     *
     * @return true if the order is currently active
     */
    public boolean isActive() {
        return this != DELIVERED && this != CANCELLED && this != REJECTED;
    }

    /**
     * Validates whether a transition from this state to the target state is permitted
     * by the system business rules.
     *
     * @param next the target status
     * @return true if transition is legally allowed, false otherwise
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case PLACED -> next == ACCEPTED || next == CANCELLED || next == REJECTED;
            case ACCEPTED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> next == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DELIVERED;
            case DELIVERED, CANCELLED, REJECTED -> false;
        };
    }
}
