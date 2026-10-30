package com.food_delivery.restaurant.model;

import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.exception.RestaurantStateException;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Model representing a Restaurant in the Food Delivery application.
 */
public class Restaurant {
    private final String id;
    private String name;
    private CuisineType cuisineType;
    private String city;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private BigDecimal minimumOrderValue;
    private boolean isActive;
    private boolean isOpen;

    public Restaurant(String id,
                      String name,
                      CuisineType cuisineType,
                      String city,
                      LocalTime openingTime,
                      LocalTime closingTime,
                      BigDecimal minimumOrderValue) {
        validateId(id);
        validateName(name);
        validateCuisineType(cuisineType);
        validateCity(city);
        validateOperatingHours(openingTime, closingTime);
        validateMinimumOrderValue(minimumOrderValue);

        this.id = id.trim();
        this.name = name.trim();
        this.cuisineType = cuisineType;
        this.city = city.trim();
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.minimumOrderValue = minimumOrderValue;
        this.isActive = false;
        this.isOpen = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.isActive) {
            throw new RestaurantStateException("Cannot modify restaurant name after activation.");
        }
        validateName(name);
        this.name = name.trim();
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(CuisineType cuisineType) {
        if (this.isActive) {
            throw new RestaurantStateException("Cannot modify cuisine type after activation.");
        }
        validateCuisineType(cuisineType);
        this.cuisineType = cuisineType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (this.isActive) {
            throw new RestaurantStateException("Cannot modify city after activation.");
        }
        validateCity(city);
        this.city = city.trim();
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setOperatingHours(LocalTime openingTime, LocalTime closingTime) {
        validateOperatingHours(openingTime, closingTime);
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        validateMinimumOrderValue(minimumOrderValue);
        this.minimumOrderValue = minimumOrderValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    /**
     * Determines whether the given time falls within the restaurant's operating hours.
     * Correctly handles both same-day schedules (e.g., 09:00 to 22:00) and
     * schedules that cross midnight (e.g., 20:00 to 02:00).
     *
     * @param currentTime the time to evaluate
     * @return true if currentTime falls within opening hours, false otherwise
     */
    public boolean isWithinOperatingHours(LocalTime currentTime) {
        if (currentTime == null) {
            throw new ValidationException("Current time cannot be null for operating hours check.");
        }

        // When opening time equals closing time, the restaurant operates 24 hours.
        if (openingTime.equals(closingTime)) {
            return true;
        }

        if (openingTime.isBefore(closingTime)) {
            // Standard same-day operating hours (e.g., 09:00 - 22:00)
            return !currentTime.isBefore(openingTime) && !currentTime.isAfter(closingTime);
        } else {
            // Operating schedule crosses midnight (e.g., 20:00 - 02:00)
            return !currentTime.isBefore(openingTime) || !currentTime.isAfter(closingTime);
        }
    }

    /**
     * Checks if the restaurant is currently accepting orders.
     * A restaurant accepts orders only when:
     * 1. It is activated.
     * 2. It is marked as Open.
     * 3. The current time falls within its opening hours.
     *
     * @param currentTime the time to check against
     * @return true if accepting orders, false otherwise
     */
    public boolean isAcceptingOrders(LocalTime currentTime) {
        if (!isActive || !isOpen) {
            return false;
        }
        return isWithinOperatingHours(currentTime);
    }

    // --- Validation Helpers ---

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Restaurant name cannot be null or blank.");
        }
    }

    private void validateCuisineType(CuisineType cuisineType) {
        if (cuisineType == null) {
            throw new ValidationException("Cuisine type cannot be null.");
        }
    }

    private void validateCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new ValidationException("City cannot be null or blank.");
        }
    }

    private void validateOperatingHours(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime == null || closingTime == null) {
            throw new ValidationException("Opening time and closing time cannot be null.");
        }
    }

    private void validateMinimumOrderValue(BigDecimal minimumOrderValue) {
        if (minimumOrderValue == null) {
            throw new ValidationException("Minimum order value cannot be null.");
        }
        if (minimumOrderValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Minimum order value cannot be negative. Provided: " + minimumOrderValue);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cuisineType=" + cuisineType +
                ", city='" + city + '\'' +
                ", openingTime=" + openingTime +
                ", closingTime=" + closingTime +
                ", minimumOrderValue=" + minimumOrderValue +
                ", isActive=" + isActive +
                ", isOpen=" + isOpen +
                '}';
    }
}

