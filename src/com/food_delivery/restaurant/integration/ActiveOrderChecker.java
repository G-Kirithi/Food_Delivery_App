package com.food_delivery.restaurant.integration;

/**
 * Integration interface for checking active orders for a restaurant.
 * The Order Management module will implement this interface.
 *
 * An active order is defined as any order whose status is NOT:
 * - DELIVERED
 * - CANCELLED
 */
public interface ActiveOrderChecker {

    /**
     * Checks if the given restaurant currently has active orders.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return true if one or more active orders exist, false otherwise
     */
    boolean hasActiveOrders(String restaurantId);
}

