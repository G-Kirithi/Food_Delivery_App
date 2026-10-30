package com.food_delivery.customer.service;

import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.exception.CustomerNotFoundException;
import com.food_delivery.customer.exception.CustomerValidationException;
import com.food_delivery.customer.exception.DuplicatePhoneNumberException;
import com.food_delivery.customer.exception.InvalidAddressException;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.restaurant.exception.DuplicateResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CustomerService handling all customer and address operations and business rules.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("CustomerRepository cannot be null.");
        }
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        if (request == null) {
            throw new CustomerValidationException("Registration request cannot be null.");
        }

        String rawPhone = request.getPhoneNumber();
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            throw new CustomerValidationException("Phone number cannot be null or empty.");
        }
        String cleanPhone = rawPhone.trim();

        if (customerRepository.existsByPhoneNumber(cleanPhone)) {
            throw new DuplicatePhoneNumberException("Customer with phone number '" + cleanPhone + "' already exists.");
        }

        String customerId = (request.getId() != null && !request.getId().trim().isEmpty())
                ? request.getId().trim()
                : "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (customerRepository.existsById(customerId)) {
            throw new DuplicateResourceException("Customer with ID '" + customerId + "' already exists.");
        }

        List<AddressRequest> addrRequests = request.getAddresses();
        if (addrRequests == null || addrRequests.isEmpty()) {
            throw new InvalidAddressException("A customer must have at least one delivery address upon registration.");
        }

        List<Address> addresses = new ArrayList<>();
        int addressIndex = 1;
        for (AddressRequest ar : addrRequests) {
            if (ar == null) {
                throw new InvalidAddressException("Delivery address details cannot be null.");
            }
            String addressId = (ar.getId() != null && !ar.getId().trim().isEmpty())
                    ? ar.getId().trim()
                    : customerId + "-ADDR-" + (addressIndex++);

            Address address = new Address(
                    addressId,
                    ar.getHouseDetails(),
                    ar.getStreet(),
                    ar.getArea(),
                    ar.getCity(),
                    ar.getState(),
                    ar.getPincode(),
                    ar.getLandmark()
            );
            addresses.add(address);
        }

        Customer customer = new Customer(
                customerId,
                request.getFullName(),
                cleanPhone,
                request.getEmail(),
                addresses
        );

        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CustomerValidationException("Customer ID cannot be null or blank.");
        }
        return customerRepository.findById(customerId.trim())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    @Override
    public Customer updateCustomer(String customerId, String fullName, String email) {
        Customer customer = getCustomerById(customerId);
        if (fullName != null) {
            customer.setFullName(fullName);
        }
        if (email != null) {
            customer.setEmail(email);
        }
        return customerRepository.save(customer);
    }

    @Override
    public Address addAddress(String customerId, AddressRequest addressRequest) {
        if (addressRequest == null) {
            throw new InvalidAddressException("Address request cannot be null.");
        }
        Customer customer = getCustomerById(customerId);

        String addressId = (addressRequest.getId() != null && !addressRequest.getId().trim().isEmpty())
                ? addressRequest.getId().trim()
                : customerId + "-ADDR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Address address = new Address(
                addressId,
                addressRequest.getHouseDetails(),
                addressRequest.getStreet(),
                addressRequest.getArea(),
                addressRequest.getCity(),
                addressRequest.getState(),
                addressRequest.getPincode(),
                addressRequest.getLandmark()
        );

        customer.addAddress(address);
        customerRepository.save(customer);
        return address;
    }

    @Override
    public List<Address> getAddresses(String customerId) {
        Customer customer = getCustomerById(customerId);
        return customer.getAddresses();
    }

    @Override
    public Address updateAddress(String customerId, String addressId, AddressRequest addressRequest) {
        if (addressRequest == null) {
            throw new InvalidAddressException("Updated address details cannot be null.");
        }
        Customer customer = getCustomerById(customerId);

        Address updatedAddress = new Address(
                addressId,
                addressRequest.getHouseDetails(),
                addressRequest.getStreet(),
                addressRequest.getArea(),
                addressRequest.getCity(),
                addressRequest.getState(),
                addressRequest.getPincode(),
                addressRequest.getLandmark()
        );

        customer.updateAddress(updatedAddress);
        customerRepository.save(customer);
        return updatedAddress;
    }

    @Override
    public void deleteAddress(String customerId, String addressId) {
        Customer customer = getCustomerById(customerId);
        customer.removeAddress(addressId);
        customerRepository.save(customer);
    }

    @Override
    public boolean existsById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsById(customerId.trim());
    }
}
