package com.food_delivery.customer.controller;

import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.exception.CustomerNotFoundException;
import com.food_delivery.customer.exception.CustomerValidationException;
import com.food_delivery.customer.exception.DuplicatePhoneNumberException;
import com.food_delivery.customer.exception.InvalidAddressException;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.service.CustomerService;
import com.food_delivery.restaurant.exception.DuplicateResourceException;

import java.util.List;

/**
 * REST API Controller layer for Customer and Delivery Address endpoints.
 */
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        if (customerService == null) {
            throw new IllegalArgumentException("CustomerService cannot be null.");
        }
        this.customerService = customerService;
    }

    /**
     * POST /api/customers/register
     */
    public ApiResponse<Customer> registerCustomer(CustomerRegistrationRequest request) {
        try {
            Customer customer = customerService.registerCustomer(request);
            return ApiResponse.created("Customer registered successfully.", customer);
        } catch (DuplicatePhoneNumberException | DuplicateResourceException e) {
            return ApiResponse.error(e.getMessage(), 409);
        } catch (CustomerValidationException | InvalidAddressException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}
     */
    public ApiResponse<Customer> getCustomer(String customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            return ApiResponse.ok("Customer retrieved successfully.", customer);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /api/customers/{customerId}
     */
    public ApiResponse<Customer> updateCustomer(String customerId, String fullName, String email) {
        try {
            Customer customer = customerService.updateCustomer(customerId, fullName, email);
            return ApiResponse.ok("Customer updated successfully.", customer);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/customers/{customerId}/addresses
     */
    public ApiResponse<Address> addAddress(String customerId, AddressRequest request) {
        try {
            Address address = customerService.addAddress(customerId, request);
            return ApiResponse.created("Address added successfully.", address);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (InvalidAddressException | CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}/addresses
     */
    public ApiResponse<List<Address>> getAddresses(String customerId) {
        try {
            List<Address> addresses = customerService.getAddresses(customerId);
            return ApiResponse.ok("Addresses retrieved successfully.", addresses);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /api/customers/{customerId}/addresses/{addressId}
     */
    public ApiResponse<Address> updateAddress(String customerId, String addressId, AddressRequest request) {
        try {
            Address address = customerService.updateAddress(customerId, addressId, request);
            return ApiResponse.ok("Address updated successfully.", address);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (InvalidAddressException | CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * DELETE /api/customers/{customerId}/addresses/{addressId}
     */
    public ApiResponse<Void> deleteAddress(String customerId, String addressId) {
        try {
            customerService.deleteAddress(customerId, addressId);
            return ApiResponse.ok("Address deleted successfully.", null);
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (InvalidAddressException | CustomerValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }
}
