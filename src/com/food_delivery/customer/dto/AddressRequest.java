package com.food_delivery.customer.dto;

/**
 * Data Transfer Object for creating or updating an Address.
 */
public class AddressRequest {
    private String id;
    private String houseDetails;
    private String street;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private String landmark;

    public AddressRequest() {
    }

    public AddressRequest(String id,
                          String houseDetails,
                          String street,
                          String area,
                          String city,
                          String state,
                          String pincode,
                          String landmark) {
        this.id = id;
        this.houseDetails = houseDetails;
        this.street = street;
        this.area = area;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.landmark = landmark;
    }

    public AddressRequest(String houseDetails,
                          String street,
                          String area,
                          String city,
                          String state,
                          String pincode,
                          String landmark) {
        this(null, houseDetails, street, area, city, state, pincode, landmark);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHouseDetails() {
        return houseDetails;
    }

    public void setHouseDetails(String houseDetails) {
        this.houseDetails = houseDetails;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}
