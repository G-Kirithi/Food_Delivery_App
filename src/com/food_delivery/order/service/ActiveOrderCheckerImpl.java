package com.food_delivery.order.service;

import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.restaurant.integration.ActiveOrderChecker;

/**
 * Implementation of ActiveOrderChecker integrating the Order Management module
 * with the Restaurant module to prevent deletion of restaurants with active orders.
 */
public class ActiveOrderCheckerImpl implements ActiveOrderChecker {

    private final OrderRepository orderRepository;

    public ActiveOrderCheckerImpl(OrderRepository orderRepository) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null.");
        }
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean hasActiveOrders(String restaurantId) {
        return orderRepository.hasActiveOrdersForRestaurant(restaurantId);
    }
}
