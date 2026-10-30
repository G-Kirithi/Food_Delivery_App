package com.food_delivery.model;

import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Compatibility class mapping com.food_delivery.model.order to com.food_delivery.order.model.Order.
 */
public class order extends Order {

    public order(String id,
                 String customerId,
                 String customerName,
                 String restaurantId,
                 String restaurantName,
                 String deliveryAddressId,
                 String deliveryCity,
                 String deliveryFullAddress,
                 List<OrderItem> items,
                 BigDecimal totalAmount) {
        super(id, customerId, customerName, restaurantId, restaurantName,
              deliveryAddressId, deliveryCity, deliveryFullAddress, items, totalAmount);
    }

    public order(String id,
                 String customerId,
                 String customerName,
                 String restaurantId,
                 String restaurantName,
                 String deliveryAddressId,
                 String deliveryCity,
                 String deliveryFullAddress,
                 List<OrderItem> items,
                 BigDecimal totalAmount,
                 Instant placedAt) {
        super(id, customerId, customerName, restaurantId, restaurantName,
              deliveryAddressId, deliveryCity, deliveryFullAddress, items, totalAmount, placedAt);
    }
}
