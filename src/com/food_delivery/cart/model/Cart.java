package com.food_delivery.cart.model;

import com.food_delivery.cart.exception.CartNotFoundException;
import com.food_delivery.cart.exception.InvalidQuantityException;
import com.food_delivery.cart.exception.RestaurantMismatchException;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Model representing a Customer's active Shopping Cart.
 */
public class Cart {
    private final String id;
    private final String customerId;
    private String restaurantId;
    private final List<CartItem> items;
    private BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart(String id, String customerId) {
        validateId(id);
        validateCustomerId(customerId);

        this.id = id.trim();
        this.customerId = customerId.trim();
        this.restaurantId = null;
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Optional<CartItem> findItemByMenuItemId(String menuItemId) {
        if (menuItemId == null) return Optional.empty();
        return items.stream()
                .filter(i -> i.getMenuItemId().equals(menuItemId))
                .findFirst();
    }

    public Optional<CartItem> findItemById(String cartItemId) {
        if (cartItemId == null) return Optional.empty();
        return items.stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst();
    }

    /**
     * Adds an item to the cart, enforcing single-restaurant constraint and max-10-units rule.
     * If the item is already in the cart, increases its quantity.
     *
     * @param targetRestaurantId restaurant ID of the item
     * @param newItem            cart item line to add
     */
    public synchronized void addItem(String targetRestaurantId, CartItem newItem) {
        if (targetRestaurantId == null || targetRestaurantId.trim().isEmpty()) {
            throw new ValidationException("Restaurant ID cannot be null or blank for adding item to cart.");
        }
        if (newItem == null) {
            throw new ValidationException("Cart item cannot be null.");
        }

        String targetRestId = targetRestaurantId.trim();

        // Enforce single-restaurant rule
        if (!items.isEmpty() && this.restaurantId != null && !this.restaurantId.equals(targetRestId)) {
            throw new RestaurantMismatchException(
                    "Your cart already contains items from restaurant ID '" + this.restaurantId +
                    "'. A cart can only contain items from ONE restaurant at a time. Requested restaurant: '" + targetRestId + "'."
            );
        }

        // Set restaurant ID if cart was empty
        if (this.restaurantId == null || items.isEmpty()) {
            this.restaurantId = targetRestId;
        }

        // Check if item already exists in cart
        Optional<CartItem> existingOpt = findItemByMenuItemId(newItem.getMenuItemId());
        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            int combinedQty = existing.getQuantity() + newItem.getQuantity();
            if (combinedQty > CartItem.MAX_ITEM_QUANTITY) {
                throw new InvalidQuantityException(
                        "Cannot add " + newItem.getQuantity() + " units. Cart already has " + existing.getQuantity() +
                        " units. Maximum allowed per item is " + CartItem.MAX_ITEM_QUANTITY + " units."
                );
            }
            existing.setQuantity(combinedQty);
        } else {
            items.add(newItem);
        }

        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the quantity of an existing item in the cart.
     *
     * @param cartItemId  cart item ID
     * @param newQuantity new quantity (1-10)
     */
    public synchronized void updateItemQuantity(String cartItemId, int newQuantity) {
        CartItem item = findItemById(cartItemId)
                .orElseThrow(() -> new CartNotFoundException("Cart item not found with ID: " + cartItemId));

        item.setQuantity(newQuantity);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes an individual item from the cart.
     * If all items are removed, resets restaurant association.
     *
     * @param cartItemId cart item ID
     */
    public synchronized void removeItem(String cartItemId) {
        if (cartItemId == null || cartItemId.trim().isEmpty()) {
            throw new ValidationException("Cart item ID cannot be null or blank.");
        }
        boolean removed = items.removeIf(i -> i.getId().equals(cartItemId.trim()));
        if (!removed) {
            throw new CartNotFoundException("Cart item not found with ID: " + cartItemId);
        }

        if (items.isEmpty()) {
            this.restaurantId = null;
        }
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Clears all items in the cart and resets restaurant association.
     */
    public synchronized void clear() {
        items.clear();
        this.restaurantId = null;
        this.totalAmount = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Recalculates total cart amount from individual item subtotals.
     */
    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItem item : items) {
            sum = sum.add(item.getSubtotal());
        }
        this.totalAmount = sum;
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Cart ID cannot be null or blank.");
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID cannot be null or blank for cart.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(id, cart.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", itemCount=" + items.size() +
                ", totalAmount=" + totalAmount +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
