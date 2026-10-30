package com.food_delivery.customer.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object for registering a new Customer.
 */
public class CustomerRegistrationRequest {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private List<AddressRequest> addresses = new ArrayList<>();

    public CustomerRegistrationRequest() {
    }

    public CustomerRegistrationRequest(String id,
                                       String fullName,
                                       String phoneNumber,
                                       String email,
                                       List<AddressRequest> addresses) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        if (addresses != null) {
            this.addresses = new ArrayList<>(addresses);
        }
    }

    public CustomerRegistrationRequest(String id,
                                       String fullName,
                                       String phoneNumber,
                                       String email,
                                       AddressRequest primaryAddress) {
        this(id, fullName, phoneNumber, email, primaryAddress != null ? Collections.singletonList(primaryAddress) : null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AddressRequest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressRequest> addresses) {
        this.addresses = (addresses != null) ? new ArrayList<>(addresses) : new ArrayList<>();
    }

    public void addAddress(AddressRequest address) {
        if (address != null) {
            this.addresses.add(address);
        }
    }
}
