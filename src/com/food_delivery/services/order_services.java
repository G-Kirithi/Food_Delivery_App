package com.food_delivery.services;

import com.food_delivery.cart.service.CartService;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.order.service.OrderServiceImpl;
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.RestaurantService;

/**
 * Compatibility service class mapping com.food_delivery.services.order_services
 * to OrderServiceImpl.
 */
public class order_services extends OrderServiceImpl {

    public order_services(OrderRepository orderRepository,
                          CustomerRepository customerRepository,
                          RestaurantService restaurantService,
                          MenuService menuService,
                          CartService cartService) {
        super(orderRepository, customerRepository, restaurantService, menuService, cartService);
    }
}
