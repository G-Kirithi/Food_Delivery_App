package com.food_delivery.restaurant.repository;

import com.food_delivery.restaurant.model.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of MenuItemRepository using ConcurrentHashMap.
 */
public class InMemoryMenuItemRepository implements MenuItemRepository {
    private final Map<String, MenuItem> store = new ConcurrentHashMap<>();

    @Override
    public MenuItem save(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null.");
        }
        store.put(menuItem.getId(), menuItem);
        return menuItem;
    }

    @Override
    public Optional<MenuItem> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<MenuItem> findByRestaurantId(String restaurantId) {
        if (restaurantId == null) {
            return Collections.emptyList();
        }
        return store.values().stream()
                .filter(item -> restaurantId.equals(item.getRestaurantId()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<MenuItem> searchByName(String restaurantId, String query) {
        if (restaurantId == null || query == null) {
            return Collections.emptyList();
        }
        String lowerQuery = query.toLowerCase();
        return store.values().stream()
                .filter(item -> restaurantId.equals(item.getRestaurantId()))
                .filter(item -> item.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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
    public void deleteByRestaurantId(String restaurantId) {
        if (restaurantId != null) {
            store.entrySet().removeIf(entry -> restaurantId.equals(entry.getValue().getRestaurantId()));
        }
    }

    @Override
    public void clear() {
        store.clear();
    }
}

