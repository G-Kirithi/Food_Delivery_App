package com.food_delivery.order.controller;

import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.order.dto.OrderResponse;
import com.food_delivery.order.dto.PlaceOrderRequest;
import com.food_delivery.order.exception.InvalidOrderStateException;
import com.food_delivery.order.exception.OrderNotFoundException;
import com.food_delivery.order.exception.OrderTimeoutException;
import com.food_delivery.order.exception.OrderValidationException;
import com.food_delivery.order.exception.UnauthorizedOrderException;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.service.OrderService;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API Controller layer for Order management endpoints.
 */
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        if (orderService == null) {
            throw new IllegalArgumentException("OrderService cannot be null.");
        }
        this.orderService = orderService;
    }

    /**
     * POST /api/orders - Places an order from the customer's active cart.
     */
    public ApiResponse<OrderResponse> placeOrder(PlaceOrderRequest request) {
        try {
            Order order = orderService.placeOrder(request);
            return ApiResponse.created("Order placed successfully.", new OrderResponse(order));
        } catch (OrderValidationException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/orders/{orderId} - Retrieves order details by order ID.
     */
    public ApiResponse<OrderResponse> getOrder(String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ApiResponse.ok("Order retrieved successfully.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (OrderValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}/orders - Retrieves all orders for a customer.
     */
    public ApiResponse<List<OrderResponse>> getCustomerOrders(String customerId) {
        try {
            List<Order> orders = orderService.getOrdersByCustomerId(customerId);
            List<OrderResponse> responseList = orders.stream().map(OrderResponse::new).collect(Collectors.toList());
            return ApiResponse.ok("Customer orders retrieved successfully.", responseList);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/customers/{customerId}/orders/active - Retrieves active orders for a customer.
     */
    public ApiResponse<List<OrderResponse>> getActiveCustomerOrders(String customerId) {
        try {
            List<Order> orders = orderService.getActiveOrdersByCustomerId(customerId);
            List<OrderResponse> responseList = orders.stream().map(OrderResponse::new).collect(Collectors.toList());
            return ApiResponse.ok("Active customer orders retrieved successfully.", responseList);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /api/restaurants/{restaurantId}/orders - Retrieves orders for a restaurant.
     */
    public ApiResponse<List<OrderResponse>> getRestaurantOrders(String restaurantId) {
        try {
            List<Order> orders = orderService.getOrdersByRestaurantId(restaurantId);
            List<OrderResponse> responseList = orders.stream().map(OrderResponse::new).collect(Collectors.toList());
            return ApiResponse.ok("Restaurant orders retrieved successfully.", responseList);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/orders/{orderId}/accept - Restaurant accepts the order.
     */
    public ApiResponse<OrderResponse> acceptOrder(String orderId, String restaurantId, UserRole role) {
        try {
            Order order = orderService.acceptOrder(orderId, restaurantId, role);
            return ApiResponse.ok("Order accepted successfully.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (UnauthorizedOrderException e) {
            return ApiResponse.error(e.getMessage(), 403);
        } catch (OrderTimeoutException e) {
            return ApiResponse.error(e.getMessage(), 408); // 408 Request Timeout
        } catch (InvalidOrderStateException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/orders/{orderId}/reject - Restaurant rejects the order with a reason.
     */
    public ApiResponse<OrderResponse> rejectOrder(String orderId, String restaurantId, String reason, UserRole role) {
        try {
            Order order = orderService.rejectOrder(orderId, restaurantId, reason, role);
            return ApiResponse.ok("Order rejected successfully.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (UnauthorizedOrderException e) {
            return ApiResponse.error(e.getMessage(), 403);
        } catch (InvalidOrderStateException | ValidationException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/orders/{orderId}/prepare - Restaurant marks order as preparing.
     */
    public ApiResponse<OrderResponse> startPreparation(String orderId, String restaurantId, UserRole role) {
        try {
            Order order = orderService.startPreparation(orderId, restaurantId, role);
            return ApiResponse.ok("Order preparation started.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (UnauthorizedOrderException e) {
            return ApiResponse.error(e.getMessage(), 403);
        } catch (InvalidOrderStateException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/orders/{orderId}/ready - Restaurant marks order ready for pickup.
     */
    public ApiResponse<OrderResponse> markReadyForPickup(String orderId, String restaurantId, UserRole role) {
        try {
            Order order = orderService.markReadyForPickup(orderId, restaurantId, role);
            return ApiResponse.ok("Order marked ready for pickup.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (UnauthorizedOrderException e) {
            return ApiResponse.error(e.getMessage(), 403);
        } catch (InvalidOrderStateException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /api/orders/{orderId}/cancel - Customer cancels the order.
     */
    public ApiResponse<OrderResponse> cancelOrder(String orderId, String customerId, String reason) {
        try {
            Order order = orderService.cancelOrder(orderId, customerId, reason);
            return ApiResponse.ok("Order cancelled successfully.", new OrderResponse(order));
        } catch (OrderNotFoundException e) {
            return ApiResponse.error(e.getMessage(), 404);
        } catch (UnauthorizedOrderException e) {
            return ApiResponse.error(e.getMessage(), 403);
        } catch (InvalidOrderStateException e) {
            return ApiResponse.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ApiResponse.error("An unexpected error occurred: " + e.getMessage(), 500);
        }
    }
}
