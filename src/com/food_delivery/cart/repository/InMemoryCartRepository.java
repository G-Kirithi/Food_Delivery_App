package com.food_delivery.cart.repository;

import com.food_delivery.cart.model.Cart;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of CartRepository using ConcurrentHashMap.
 */
public class InMemoryCartRepository implements CartRepository {

    // Keyed by customerId for O(1) active customer cart lookup
    private final Map<String, Cart> storageByCustomerId = new ConcurrentHashMap<>();
    // Secondary index by cartId
    private final Map<String, String> cartIdToCustomerId = new ConcurrentHashMap<>();

    @Override
    public Cart save(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart to save cannot be null.");
        }
        storageByCustomerId.put(cart.getCustomerId(), cart);
        cartIdToCustomerId.put(cart.getId(), cart.getCustomerId());
        return cart;
    }

    @Override
    public Optional<Cart> findById(String id) {
        if (id == null) return Optional.empty();
        String customerId = cartIdToCustomerId.get(id);
        if (customerId == null) return Optional.empty();
        return Optional.ofNullable(storageByCustomerId.get(customerId));
    }

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        if (customerId == null) return Optional.empty();
        return Optional.ofNullable(storageByCustomerId.get(customerId));
    }

    @Override
    public boolean existsByCustomerId(String customerId) {
        if (customerId == null) return false;
        return storageByCustomerId.containsKey(customerId);
    }

    @Override
    public void deleteByCustomerId(String customerId) {
        if (customerId != null) {
            Cart removed = storageByCustomerId.remove(customerId);
            if (removed != null) {
                cartIdToCustomerId.remove(removed.getId());
            }
        }
    }

    @Override
    public void clear() {
        storageByCustomerId.clear();
        cartIdToCustomerId.clear();
    }
}
