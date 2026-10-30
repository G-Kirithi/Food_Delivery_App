package com.food_delivery.restaurant.service;

import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.DuplicateResourceException;
import com.food_delivery.restaurant.exception.ResourceNotFoundException;
import com.food_delivery.restaurant.exception.RestaurantStateException;
import com.food_delivery.restaurant.exception.UnauthorizedException;
import com.food_delivery.restaurant.exception.ValidationException;
import com.food_delivery.restaurant.integration.ActiveOrderChecker;
import com.food_delivery.restaurant.model.Restaurant;
import com.food_delivery.restaurant.repository.MenuItemRepository;
import com.food_delivery.restaurant.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Default implementation of RestaurantService containing business logic for Restaurant Management.
 */
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private ActiveOrderChecker activeOrderChecker;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository) {
        this(restaurantRepository, menuItemRepository, null);
    }

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository,
                                 ActiveOrderChecker activeOrderChecker) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException("RestaurantRepository cannot be null.");
        }
        if (menuItemRepository == null) {
            throw new IllegalArgumentException("MenuItemRepository cannot be null.");
        }
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.activeOrderChecker = activeOrderChecker;
    }

    @Override
    public void setActiveOrderChecker(ActiveOrderChecker activeOrderChecker) {
        this.activeOrderChecker = activeOrderChecker;
    }

    @Override
    public Restaurant addRestaurant(Restaurant restaurant, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "create a restaurant");
        if (restaurant == null) {
            throw new ValidationException("Restaurant entity cannot be null.");
        }

        if (restaurantRepository.existsById(restaurant.getId())) {
            throw new DuplicateResourceException("Restaurant with ID '" + restaurant.getId() + "' already exists.");
        }

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant getRestaurantById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank.");
        }
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
    }

    @Override
    public List<Restaurant> listRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    public Restaurant activateRestaurant(String id, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "activate a restaurant");
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setActive(true);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant markOpen(String id, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "mark restaurant open");
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setOpen(true);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant markClosed(String id, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "mark restaurant closed");
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setOpen(false);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateOperatingHours(String id, LocalTime openingTime, LocalTime closingTime, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update restaurant operating hours");
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setOperatingHours(openingTime, closingTime);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateMinimumOrderValue(String id, BigDecimal minimumOrderValue, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update restaurant minimum order value");
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setMinimumOrderValue(minimumOrderValue);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurantDetails(String id, String name, CuisineType cuisine, String city, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "update restaurant details");
        Restaurant restaurant = getRestaurantById(id);

        if (restaurant.isActive()) {
            throw new RestaurantStateException("Cannot modify name, cuisine, or city after restaurant activation.");
        }

        restaurant.setName(name);
        restaurant.setCuisineType(cuisine);
        restaurant.setCity(city);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(String id, UserRole role) {
        checkAuthorizedAdminOrOwner(role, "delete a restaurant");
        // Verify restaurant exists
        getRestaurantById(id);

        if (activeOrderChecker == null) {
            throw new RestaurantStateException(
                    "Cannot verify active order status: ActiveOrderChecker is not configured. Restaurant deletion cannot proceed safely."
            );
        }

        if (activeOrderChecker.hasActiveOrders(id)) {
            throw new RestaurantStateException(
                    "Cannot delete restaurant with ID '" + id + "' because it has active orders. Orders must be DELIVERED or CANCELLED."
            );
        }

        // Delete associated menu items
        menuItemRepository.deleteByRestaurantId(id);
        // Delete restaurant
        restaurantRepository.deleteById(id);
    }

    @Override
    public boolean isAcceptingOrders(String id, LocalTime currentTime) {
        Restaurant restaurant = getRestaurantById(id);
        return restaurant.isAcceptingOrders(currentTime);
    }

    @Override
    public BigDecimal getMinimumOrderValue(String id) {
        Restaurant restaurant = getRestaurantById(id);
        return restaurant.getMinimumOrderValue();
    }

    @Override
    public boolean isRestaurantActive(String id) {
        Restaurant restaurant = getRestaurantById(id);
        return restaurant.isActive();
    }

    @Override
    public boolean isRestaurantOpen(String id) {
        Restaurant restaurant = getRestaurantById(id);
        return restaurant.isOpen();
    }

    private void checkAuthorizedAdminOrOwner(UserRole role, String action) {
        if (role == null) {
            throw new UnauthorizedException("User role cannot be null. Authorization failed to " + action + ".");
        }
        if (role == UserRole.CUSTOMER) {
            throw new UnauthorizedException("User with role CUSTOMER is not authorized to " + action + ".");
        }
        if (role != UserRole.ADMIN && role != UserRole.RESTAURANT_OWNER) {
            throw new UnauthorizedException("Unauthorized role '" + role + "' cannot " + action + ".");
        }
    }
}

