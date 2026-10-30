package com.food_delivery.restaurant.repository;

import com.food_delivery.restaurant.model.Restaurant;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for persisting and querying Restaurant entities.
 */
public interface RestaurantRepository {

    /**
     * Saves or updates a restaurant.
     *
     * @param restaurant the restaurant entity to save
     * @return the saved restaurant
     */
    Restaurant save(Restaurant restaurant);

    /**
     * Finds a restaurant by its unique identifier.
     *
     * @param id restaurant ID
     * @return an Optional containing the restaurant if found, or empty Optional
     */
    Optional<Restaurant> findById(String id);

    /**
     * Retrieves all restaurants.
     *
     * @return list of all restaurants
     */
    List<Restaurant> findAll();

    /**
     * Checks if a restaurant with the given ID exists.
     *
     * @param id restaurant ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes a restaurant by its ID.
     *
     * @param id restaurant ID
     */
    void deleteById(String id);

    /**
     * Clears all stored restaurants (useful for test resets).
     */
    void clear();
}

