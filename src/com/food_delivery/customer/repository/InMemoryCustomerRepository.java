package com.food_delivery.customer.repository;

import com.food_delivery.customer.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of CustomerRepository using ConcurrentHashMap.
 */
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> storage = new ConcurrentHashMap<>();

    @Override
    public Customer save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer entity to save cannot be null.");
        }
        storage.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return Optional.empty();
        }
        String cleanPhone = phoneNumber.trim();
        return storage.values().stream()
                .filter(c -> c.getPhoneNumber().equals(cleanPhone))
                .findFirst();
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(id);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        String cleanPhone = phoneNumber.trim();
        return storage.values().stream()
                .anyMatch(c -> c.getPhoneNumber().equals(cleanPhone));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
