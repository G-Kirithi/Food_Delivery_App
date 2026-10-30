package com.food_delivery.customer.service;

import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;

import java.util.List;

/**
 * Service contract for Customer registration and profile/address management.
 */
public interface CustomerService {

    /**
     * Registers a new customer with profile details and at least one delivery address.
     *
     * @param request customer registration details
     * @return the newly registered customer
     */
    Customer registerCustomer(CustomerRegistrationRequest request);

    /**
     * Retrieves a customer by their unique ID.
     *
     * @param customerId unique customer identifier
     * @return found customer entity
     */
    Customer getCustomerById(String customerId);

    /**
     * Updates customer name and email.
     *
     * @param customerId unique customer identifier
     * @param fullName   new full name
     * @param email      new email address
     * @return updated customer entity
     */
    Customer updateCustomer(String customerId, String fullName, String email);

    /**
     * Adds an additional delivery address to an existing customer.
     *
     * @param customerId     unique customer identifier
     * @param addressRequest address details
     * @return the created address
     */
    Address addAddress(String customerId, AddressRequest addressRequest);

    /**
     * Retrieves all delivery addresses registered for a customer.
     *
     * @param customerId unique customer identifier
     * @return list of delivery addresses
     */
    List<Address> getAddresses(String customerId);

    /**
     * Updates an existing delivery address.
     *
     * @param customerId     unique customer identifier
     * @param addressId      address ID to update
     * @param addressRequest new address details
     * @return updated address
     */
    Address updateAddress(String customerId, String addressId, AddressRequest addressRequest);

    /**
     * Deletes a delivery address.
     * Fails if this is the customer's last remaining delivery address.
     *
     * @param customerId unique customer identifier
     * @param addressId  address ID to delete
     */
    void deleteAddress(String customerId, String addressId);

    /**
     * Checks if a customer exists with the given ID.
     *
     * @param customerId unique customer identifier
     * @return true if exists, false otherwise
     */
    boolean existsById(String customerId);
}
