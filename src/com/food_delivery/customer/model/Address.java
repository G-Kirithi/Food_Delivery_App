package com.food_delivery.customer.model;

import com.food_delivery.customer.exception.InvalidAddressException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Model representing a Customer's Delivery Address.
 */
public class Address {
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^\\d{6}$");

    private final String id;
    private String houseDetails;
    private String street;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private String landmark;

    public Address(String id,
                   String houseDetails,
                   String street,
                   String area,
                   String city,
                   String state,
                   String pincode,
                   String landmark) {
        validateId(id);
        validateHouseDetails(houseDetails);
        validateStreet(street);
        validateArea(area);
        validateCity(city);
        validateState(state);
        validatePincode(pincode);

        this.id = id.trim();
        this.houseDetails = houseDetails.trim();
        this.street = street.trim();
        this.area = area.trim();
        this.city = city.trim();
        this.state = state.trim();
        this.pincode = pincode.trim();
        this.landmark = (landmark != null) ? landmark.trim() : "";
    }

    public Address(String id,
                   String houseDetails,
                   String street,
                   String area,
                   String city,
                   String state,
                   String pincode) {
        this(id, houseDetails, street, area, city, state, pincode, null);
    }

    public String getId() {
        return id;
    }

    public String getHouseDetails() {
        return houseDetails;
    }

    public void setHouseDetails(String houseDetails) {
        validateHouseDetails(houseDetails);
        this.houseDetails = houseDetails.trim();
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        validateStreet(street);
        this.street = street.trim();
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        validateArea(area);
        this.area = area.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        validateCity(city);
        this.city = city.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        validateState(state);
        this.state = state.trim();
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        validatePincode(pincode);
        this.pincode = pincode.trim();
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = (landmark != null) ? landmark.trim() : "";
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidAddressException("Address ID cannot be null or blank.");
        }
    }

    private void validateHouseDetails(String houseDetails) {
        if (houseDetails == null || houseDetails.trim().isEmpty()) {
            throw new InvalidAddressException("House/building details cannot be null or blank.");
        }
    }

    private void validateStreet(String street) {
        if (street == null || street.trim().isEmpty()) {
            throw new InvalidAddressException("Street cannot be null or blank.");
        }
    }

    private void validateArea(String area) {
        if (area == null || area.trim().isEmpty()) {
            throw new InvalidAddressException("Area cannot be null or blank.");
        }
    }

    private void validateCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new InvalidAddressException("City cannot be null or blank.");
        }
    }

    private void validateState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new InvalidAddressException("State cannot be null or blank.");
        }
    }

    private void validatePincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            throw new InvalidAddressException("Pincode cannot be null or blank.");
        }
        if (!PINCODE_PATTERN.matcher(pincode.trim()).matches()) {
            throw new InvalidAddressException("Pincode must be a 6-digit numeric string. Provided: " + pincode);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", houseDetails='" + houseDetails + '\'' +
                ", street='" + street + '\'' +
                ", area='" + area + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pincode='" + pincode + '\'' +
                ", landmark='" + landmark + '\'' +
                '}';
    }
}
