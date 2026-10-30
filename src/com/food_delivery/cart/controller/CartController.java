package com.food_delivery.cart.controller;

import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.cart.dto.AddToCartRequest;
import com.food_delivery.cart.dto.CartResponse;
import com.food_delivery.cart.dto.UpdateCartItemRequest;
import com.food_delivery.cart.exception.CartNotFoundException;
import com.food_delivery.cart.exception.InvalidQuantityException;
import com.food_delivery.cart.exception.MenuItemUnavailableException;
import com.food_delivery.cart.exception.RestaurantMismatchException;
import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.customer.exception.CustomerNotFoundException;
import com.food_delivery.restaurant.exception.ResourceNotFoundException;
import com.food_delivery.restaurant.exception.ValidationException;

/**
 * REST API Controller layer for Cart endpoints.
 */
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        if (cartService == null) {
            throw new IllegalArgumentException("CartService cannot be null.");
        }
        this.cartService = cartService;
    }

    /**
     * POST /api/customers/{customerId}/cart/items
     */
    public ApiResponse<CartResponse> addItem(String customerId, AddToCartRequest request) {
        try {
            if (request != null && (request.getCustomerId() == null || request.getCustomerId().isEmpty())) {
                request.setCustomerId(customerId);
            }
            Cart updated = cartService.addItemToCart(request);
            return ApiResponse.ok("Item added to cart successfully.", new CartResponse(updated));
        } catch (CustomerNotFoundException | ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (RestaurantMismatchException e) {
            return ApiResponse.error(e.getMessage(), 409);
        } catch (MenuItemUnavailableException | InvalidQuantityException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}/cart
     */
    public ApiResponse<CartResponse> getCart(String customerId) {
        try {
            Cart cart = cartService.getOrCreateCart(customerId);
            return ApiResponse.ok("Cart retrieved successfully.", new CartResponse(cart));
        } catch (CustomerNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /api/customers/{customerId}/cart/items/{cartItemId}
     */
    public ApiResponse<CartResponse> updateItemQuantity(String customerId, String cartItemId, UpdateCartItemRequest request) {
        try {
            if (request == null) {
                return ApiResponse.error("Request body cannot be null.", 400);
            }
            Cart updated = cartService.updateCartItemQuantity(customerId, cartItemId, request.getQuantity());
            return ApiResponse.ok("Cart item quantity updated successfully.", new CartResponse(updated));
        } catch (CustomerNotFoundException | CartNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (InvalidQuantityException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * DELETE /api/customers/{customerId}/cart/items/{cartItemId}
     */
    public ApiResponse<CartResponse> removeItem(String customerId, String cartItemId) {
        try {
            Cart updated = cartService.removeCartItem(customerId, cartItemId);
            return ApiResponse.ok("Cart item removed successfully.", new CartResponse(updated));
        } catch (CustomerNotFoundException | CartNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * DELETE /api/customers/{customerId}/cart
     */
    public ApiResponse<CartResponse> clearCart(String customerId) {
        try {
            Cart cleared = cartService.clearCart(customerId);
            return ApiResponse.ok("Cart cleared successfully.", new CartResponse(cleared));
        } catch (CustomerNotFoundException | CartNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }
}
