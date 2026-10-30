package com.food_delivery.restaurant.repository;

import com.food_delivery.restaurant.model.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of RestaurantRepository using ConcurrentHashMap.
 */
public class InMemoryRestaurantRepository implements RestaurantRepository {
    private final Map<String, Restaurant> store = new ConcurrentHashMap<>();

    @Override
    public Restaurant save(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null.");
        }
        store.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Restaurant> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return store.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            store.remove(id);
        }
    }

    @Override
    public void clear() {
        store.clear();
    }
}

