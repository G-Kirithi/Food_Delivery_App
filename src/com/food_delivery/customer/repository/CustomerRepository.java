package com.food_delivery.customer.repository;

import com.food_delivery.customer.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer persistence operations.
 */
public interface CustomerRepository {

    /**
     * Saves or updates a customer entity.
     *
     * @param customer the customer to save
     * @return the saved customer
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by unique ID.
     *
     * @param id customer ID
     * @return Optional containing the customer if found, or empty
     */
    Optional<Customer> findById(String id);

    /**
     * Finds a customer by their phone number.
     *
     * @param phoneNumber phone number
     * @return Optional containing customer if found, or empty
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * Checks if a customer exists with the given ID.
     *
     * @param id customer ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Checks if any customer exists with the given phone number.
     *
     * @param phoneNumber phone number
     * @return true if exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Lists all customers.
     *
     * @return list of customers
     */
    List<Customer> findAll();

    /**
     * Deletes a customer by ID.
     *
     * @param id customer ID
     */
    void deleteById(String id);

    /**
     * Clears all customer records (useful for test isolation).
     */
    void clear();
}
