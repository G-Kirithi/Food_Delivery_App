package com.food_delivery.customer.model;

import com.food_delivery.customer.exception.CustomerValidationException;
import com.food_delivery.customer.exception.InvalidAddressException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Model representing a Customer in the Food Delivery Application.
 */
public class Customer {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,13}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private final List<Address> addresses;

    public Customer(String id,
                    String fullName,
                    String phoneNumber,
                    String email,
                    List<Address> addresses) {
        validateId(id);
        validateFullName(fullName);
        validatePhoneNumber(phoneNumber);
        validateEmail(email);
        validateInitialAddresses(addresses);

        this.id = id.trim();
        this.fullName = fullName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.email = email.trim();
        this.addresses = new ArrayList<>(addresses);
    }

    public Customer(String id,
                    String fullName,
                    String phoneNumber,
                    String email,
                    Address primaryAddress) {
        this(id, fullName, phoneNumber, email, primaryAddress != null ? Collections.singletonList(primaryAddress) : null);
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        validateFullName(fullName);
        this.fullName = fullName.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.phoneNumber = phoneNumber.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email.trim();
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Optional<Address> getAddressById(String addressId) {
        if (addressId == null) return Optional.empty();
        return addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst();
    }

    public void addAddress(Address address) {
        if (address == null) {
            throw new InvalidAddressException("Address cannot be null.");
        }
        boolean exists = addresses.stream().anyMatch(a -> a.getId().equals(address.getId()));
        if (exists) {
            throw new InvalidAddressException("Address with ID '" + address.getId() + "' already exists for this customer.");
        }
        this.addresses.add(address);
    }

    public void updateAddress(Address updatedAddress) {
        if (updatedAddress == null) {
            throw new InvalidAddressException("Updated address cannot be null.");
        }
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getId().equals(updatedAddress.getId())) {
                addresses.set(i, updatedAddress);
                return;
            }
        }
        throw new InvalidAddressException("Cannot update address. Address ID '" + updatedAddress.getId() + "' not found.");
    }

    public void removeAddress(String addressId) {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new InvalidAddressException("Address ID cannot be null or blank for removal.");
        }
        if (addresses.size() <= 1) {
            throw new InvalidAddressException("Cannot delete the last remaining delivery address. A customer must have at least one delivery address.");
        }
        boolean removed = addresses.removeIf(a -> a.getId().equals(addressId));
        if (!removed) {
            throw new InvalidAddressException("Address not found with ID: " + addressId);
        }
    }

    // --- Validation Helpers ---

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new CustomerValidationException("Customer ID cannot be null or blank.");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new CustomerValidationException("Customer full name cannot be empty.");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new CustomerValidationException("Phone number cannot be null or empty.");
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            throw new CustomerValidationException("Invalid phone number format. Provided: " + phoneNumber);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new CustomerValidationException("Email address cannot be null or empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new CustomerValidationException("Invalid email format. Provided: " + email);
        }
    }

    private void validateInitialAddresses(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            throw new InvalidAddressException("Customer must have at least one delivery address upon registration.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", addressCount=" + addresses.size() +
                '}';
    }
}
