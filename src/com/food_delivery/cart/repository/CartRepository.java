package com.food_delivery.cart.repository;

import com.food_delivery.cart.model.Cart;

import java.util.Optional;

/**
 * Repository interface for Cart persistence operations.
 */
public interface CartRepository {

    /**
     * Saves or updates a customer's cart.
     *
     * @param cart the cart to save
     * @return the saved cart
     */
    Cart save(Cart cart);

    /**
     * Finds a cart by its cart ID.
     *
     * @param id cart ID
     * @return Optional containing the cart if found, or empty
     */
    Optional<Cart> findById(String id);

    /**
     * Finds a cart by the customer's unique ID.
     *
     * @param customerId customer ID
     * @return Optional containing the cart if found, or empty
     */
    Optional<Cart> findByCustomerId(String customerId);

    /**
     * Checks if a cart exists for the given customer ID.
     *
     * @param customerId customer ID
     * @return true if exists, false otherwise
     */
    boolean existsByCustomerId(String customerId);

    /**
     * Deletes a cart by customer ID.
     *
     * @param customerId customer ID
     */
    void deleteByCustomerId(String customerId);

    /**
     * Clears all stored carts (useful for test resets).
     */
    void clear();
}
