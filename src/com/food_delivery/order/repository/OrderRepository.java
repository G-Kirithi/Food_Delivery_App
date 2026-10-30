package com.food_delivery.order.repository;

import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining persistence operations for Order entities.
 */
public interface OrderRepository {

    /**
     * Saves or updates an order in the repository.
     *
     * @param order the order entity to store
     * @return the saved order
     */
    Order save(Order order);

    /**
     * Retrieves an order by its unique ID.
     *
     * @param id order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(String id);

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all orders
     */
    List<Order> findAll();

    /**
     * Retrieves all orders placed by a specific customer.
     *
     * @param customerId customer ID
     * @return list of orders placed by the customer
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Retrieves all orders received by a specific restaurant.
     *
     * @param restaurantId restaurant ID
     * @return list of orders for the restaurant
     */
    List<Order> findByRestaurantId(String restaurantId);

    /**
     * Retrieves active orders for a customer (orders not DELIVERED, CANCELLED, or REJECTED).
     *
     * @param customerId customer ID
     * @return list of active orders
     */
    List<Order> findActiveOrdersByCustomerId(String customerId);

    /**
     * Counts the number of currently active orders for a customer.
     * Used to enforce the maximum 3 active orders rule.
     *
     * @param customerId customer ID
     * @return active order count
     */
    int countActiveOrdersByCustomerId(String customerId);

    /**
     * Checks if a restaurant currently has active orders.
     * Used by Module 1 to prevent restaurant deletion.
     *
     * @param restaurantId restaurant ID
     * @return true if one or more active orders exist, false otherwise
     */
    boolean hasActiveOrdersForRestaurant(String restaurantId);

    /**
     * Retrieves orders filtered by status.
     *
     * @param status order status
     * @return list of matching orders
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Checks whether an order with the given ID exists.
     *
     * @param id order ID
     * @return true if exists
     */
    boolean existsById(String id);

    /**
     * Deletes an order by ID (used for testing and maintenance).
     *
     * @param id order ID
     */
    void deleteById(String id);

    /**
     * Clears all order records from repository (primarily for test resets).
     */
    void clear();
}
