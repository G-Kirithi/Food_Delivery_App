package com.food_delivery.cart.service;

import com.food_delivery.cart.dto.AddToCartRequest;
import com.food_delivery.cart.model.Cart;

/**
 * Service contract for Cart operations and business rules.
 */
public interface CartService {

    /**
     * Adds an item to the customer's cart after validating restaurant, menu item,
     * availability, price, and single-restaurant constraint.
     *
     * @param request details of item to add
     * @return updated cart
     */
    Cart addItemToCart(AddToCartRequest request);

    /**
     * Overloaded convenience method to add an item to cart.
     *
     * @param customerId   customer ID
     * @param restaurantId restaurant ID
     * @param menuItemId   menu item ID
     * @param quantity     quantity to add (1-10)
     * @return updated cart
     */
    Cart addItemToCart(String customerId, String restaurantId, String menuItemId, int quantity);

    /**
     * Retrieves the active cart for a customer, creating a new empty cart if none exists.
     *
     * @param customerId customer ID
     * @return active cart
     */
    Cart getOrCreateCart(String customerId);

    /**
     * Retrieves the cart for a customer.
     *
     * @param customerId customer ID
     * @return cart entity
     */
    Cart getCartByCustomerId(String customerId);

    /**
     * Retrieves a cart by its unique cart ID.
     *
     * @param cartId cart ID
     * @return cart entity
     */
    Cart getCartById(String cartId);

    /**
     * Updates the quantity of a specific cart item.
     * Valid quantity is strictly between 1 and 10.
     *
     * @param customerId  customer ID
     * @param cartItemId  cart item ID
     * @param newQuantity new quantity
     * @return updated cart
     */
    Cart updateCartItemQuantity(String customerId, String cartItemId, int newQuantity);

    /**
     * Removes an individual item from the cart.
     * Resets the restaurant association if all items are removed.
     *
     * @param customerId customer ID
     * @param cartItemId cart item ID
     * @return updated cart
     */
    Cart removeCartItem(String customerId, String cartItemId);

    /**
     * Clears the entire cart, resetting restaurant association and total amount to 0.
     * Designed as a clean integration method for Module 3 (Order Placement).
     *
     * @param customerId customer ID
     * @return cleared cart
     */
    Cart clearCart(String customerId);
}
