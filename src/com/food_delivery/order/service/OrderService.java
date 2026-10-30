package com.food_delivery.order.service;

import com.food_delivery.order.dto.PlaceOrderRequest;
import com.food_delivery.order.model.Order;
import com.food_delivery.restaurant.enums.UserRole;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * Service contract for Order Placement, Status Management, and Lifecycle Transitions.
 */
public interface OrderService {

    /**
     * Places a new order from the customer's active cart.
     * Enforces restaurant operating status, item availability, minimum order value,
     * city serviceability, and active order limits. Clears cart upon success.
     *
     * @param request place order request containing customer and address IDs
     * @return created Order entity in PLACED status
     */
    Order placeOrder(PlaceOrderRequest request);

    /**
     * Convenience method to place an order.
     *
     * @param customerId        customer ID
     * @param deliveryAddressId delivery address ID
     * @return created Order entity
     */
    Order placeOrder(String customerId, String deliveryAddressId);

    /**
     * Places an order evaluating restaurant operating hours against a specified time.
     * Particularly useful for deterministic integration and test scenarios.
     *
     * @param customerId        customer ID
     * @param deliveryAddressId delivery address ID
     * @param evaluationTime    time to evaluate operating hours against
     * @return created Order entity
     */
    Order placeOrder(String customerId, String deliveryAddressId, LocalTime evaluationTime);

    /**
     * Restaurant accepts an order in PLACED status.
     * Must occur within 5 minutes of placement; otherwise triggers auto-cancellation.
     *
     * @param orderId      order ID
     * @param restaurantId restaurant ID of the accepting owner
     * @param role         caller role (ADMIN or RESTAURANT_OWNER)
     * @return updated Order in ACCEPTED status
     */
    Order acceptOrder(String orderId, String restaurantId, UserRole role);

    /**
     * Overloaded acceptance method allowing simulated acceptance timestamp
     * for deterministic testing of the 5-minute auto-cancel window.
     *
     * @param orderId      order ID
     * @param restaurantId restaurant ID
     * @param role         caller role
     * @param acceptTime   simulated instant of acceptance
     * @return updated Order
     */
    Order acceptOrder(String orderId, String restaurantId, UserRole role, Instant acceptTime);

    /**
     * Restaurant rejects an order in PLACED status with a mandatory reason.
     *
     * @param orderId      order ID
     * @param restaurantId restaurant ID
     * @param reason       mandatory non-blank rejection reason
     * @param role         caller role
     * @return updated Order in REJECTED status
     */
    Order rejectOrder(String orderId, String restaurantId, String reason, UserRole role);

    /**
     * Restaurant moves an accepted order to PREPARING status.
     * Once preparation starts, customer cancellation is strictly prohibited.
     *
     * @param orderId      order ID
     * @param restaurantId restaurant ID
     * @param role         caller role
     * @return updated Order in PREPARING status
     */
    Order startPreparation(String orderId, String restaurantId, UserRole role);

    /**
     * Restaurant moves a preparing order to READY_FOR_PICKUP status.
     *
     * @param orderId      order ID
     * @param restaurantId restaurant ID
     * @param role         caller role
     * @return updated Order in READY_FOR_PICKUP status
     */
    Order markReadyForPickup(String orderId, String restaurantId, UserRole role);

    /**
     * Marks an order as OUT_FOR_DELIVERY when assigned/picked up by delivery partner.
     *
     * @param orderId           order ID
     * @param deliveryPartnerId delivery partner ID
     * @return updated Order in OUT_FOR_DELIVERY status
     */
    Order markOutForDelivery(String orderId, String deliveryPartnerId);

    /**
     * Marks an order as DELIVERED upon completion.
     *
     * @param orderId order ID
     * @return updated Order in DELIVERED status
     */
    Order markDelivered(String orderId);

    /**
     * Customer cancels an order.
     * Permitted ONLY when order is in PLACED or ACCEPTED status.
     * Prohibited once status is PREPARING.
     *
     * @param orderId    order ID
     * @param customerId customer ID requesting cancellation
     * @param reason     cancellation reason
     * @return updated Order in CANCELLED status
     */
    Order cancelOrder(String orderId, String customerId, String reason);

    /**
     * Scans all PLACED orders and auto-cancels any that have exceeded the 5-minute
     * restaurant acceptance threshold.
     *
     * @return count of orders auto-cancelled
     */
    int checkAndTriggerAutoCancellations();

    /**
     * Retrieves an order by ID.
     *
     * @param orderId order ID
     * @return Order entity
     */
    Order getOrderById(String orderId);

    /**
     * Retrieves all orders for a customer.
     *
     * @param customerId customer ID
     * @return list of orders
     */
    List<Order> getOrdersByCustomerId(String customerId);

    /**
     * Retrieves all orders received by a restaurant.
     *
     * @param restaurantId restaurant ID
     * @return list of orders
     */
    List<Order> getOrdersByRestaurantId(String restaurantId);

    /**
     * Retrieves currently active orders for a customer.
     *
     * @param customerId customer ID
     * @return list of active orders
     */
    List<Order> getActiveOrdersByCustomerId(String customerId);

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all orders
     */
    List<Order> getAllOrders();
}
