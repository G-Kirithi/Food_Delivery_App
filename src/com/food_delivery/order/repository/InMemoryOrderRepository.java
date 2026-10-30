package com.food_delivery.order.repository;

import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.model.Order;
import com.food_delivery.restaurant.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of OrderRepository using ConcurrentHashMap.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new ValidationException("Order cannot be null for saving.");
        }
        orderStore.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(orderStore.get(id.trim()));
    }

    @Override
    public List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(orderStore.values()));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanId = customerId.trim();
        return orderStore.values().stream()
                .filter(o -> o.getCustomerId().equals(cleanId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<Order> findByRestaurantId(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanId = restaurantId.trim();
        return orderStore.values().stream()
                .filter(o -> o.getRestaurantId().equals(cleanId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<Order> findActiveOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanId = customerId.trim();
        return orderStore.values().stream()
                .filter(o -> o.getCustomerId().equals(cleanId) && o.isActive())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public int countActiveOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return 0;
        }
        String cleanId = customerId.trim();
        return (int) orderStore.values().stream()
                .filter(o -> o.getCustomerId().equals(cleanId) && o.isActive())
                .count();
    }

    @Override
    public boolean hasActiveOrdersForRestaurant(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            return false;
        }
        String cleanId = restaurantId.trim();
        return orderStore.values().stream()
                .anyMatch(o -> o.getRestaurantId().equals(cleanId) && o.isActive());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return orderStore.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return orderStore.containsKey(id.trim());
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            orderStore.remove(id.trim());
        }
    }

    @Override
    public void clear() {
        orderStore.clear();
    }
}
