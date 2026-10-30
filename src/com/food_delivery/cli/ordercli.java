package com.food_delivery.cli;

import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;
import com.food_delivery.order.service.OrderService;
import com.food_delivery.restaurant.enums.UserRole;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based CLI interface for demonstrating and interacting with Module 3 Order operations.
 */
public class ordercli {

    private final OrderService orderService;
    private final Scanner scanner;

    public ordercli(OrderService orderService) {
        this.orderService = orderService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("Enter choice (1-10): ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> handlePlaceOrder();
                    case "2" -> handleViewOrder();
                    case "3" -> handleViewCustomerOrders();
                    case "4" -> handleViewRestaurantOrders();
                    case "5" -> handleAcceptOrder();
                    case "6" -> handleRejectOrder();
                    case "7" -> handleStartPreparation();
                    case "8" -> handleMarkReadyForPickup();
                    case "9" -> handleCancelOrder();
                    case "10" -> {
                        System.out.println("Exiting Order CLI...");
                        exit = true;
                    }
                    default -> System.out.println("Invalid selection. Please choose 1-10.");
                }
            } catch (Exception e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=================================================");
        System.out.println("          MODULE 3: ORDER MANAGEMENT CLI         ");
        System.out.println("=================================================");
        System.out.println("1. Place Order (From Active Cart)");
        System.out.println("2. View Order Details by Order ID");
        System.out.println("3. View Orders for Customer");
        System.out.println("4. View Orders for Restaurant");
        System.out.println("5. Accept Order (Restaurant Owner / Admin)");
        System.out.println("6. Reject Order (Restaurant Owner / Admin - requires reason)");
        System.out.println("7. Start Preparation (Restaurant Owner / Admin)");
        System.out.println("8. Mark Ready for Pickup (Restaurant Owner / Admin)");
        System.out.println("9. Cancel Order (Customer - Placed/Accepted only)");
        System.out.println("10. Exit CLI");
        System.out.println("=================================================");
    }

    private void handlePlaceOrder() {
        System.out.print("Enter Customer ID: ");
        String customerId = scanner.nextLine().trim();
        System.out.print("Enter Delivery Address ID: ");
        String addressId = scanner.nextLine().trim();

        Order order = orderService.placeOrder(customerId, addressId);
        System.out.println("\n>>> Order successfully placed!");
        displayOrderSummary(order);
    }

    private void handleViewOrder() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderService.getOrderById(orderId);
        displayOrderSummary(order);
    }

    private void handleViewCustomerOrders() {
        System.out.print("Enter Customer ID: ");
        String customerId = scanner.nextLine().trim();
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        if (orders.isEmpty()) {
            System.out.println("No orders found for customer: " + customerId);
        } else {
            System.out.println("Found " + orders.size() + " orders for customer:");
            for (Order o : orders) {
                System.out.println(" - " + o.getId() + " | Status: " + o.getStatus() + " | Total: Rs." + o.getTotalAmount());
            }
        }
    }

    private void handleViewRestaurantOrders() {
        System.out.print("Enter Restaurant ID: ");
        String restaurantId = scanner.nextLine().trim();
        List<Order> orders = orderService.getOrdersByRestaurantId(restaurantId);
        if (orders.isEmpty()) {
            System.out.println("No orders found for restaurant: " + restaurantId);
        } else {
            System.out.println("Found " + orders.size() + " orders for restaurant:");
            for (Order o : orders) {
                System.out.println(" - " + o.getId() + " | Status: " + o.getStatus() + " | Total: Rs." + o.getTotalAmount());
            }
        }
    }

    private void handleAcceptOrder() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        System.out.print("Enter Restaurant ID: ");
        String restaurantId = scanner.nextLine().trim();

        Order order = orderService.acceptOrder(orderId, restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println("\n>>> Order accepted!");
        System.out.println("Order ID: " + order.getId() + " | New Status: " + order.getStatus());
    }

    private void handleRejectOrder() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        System.out.print("Enter Restaurant ID: ");
        String restaurantId = scanner.nextLine().trim();
        System.out.print("Enter Rejection Reason: ");
        String reason = scanner.nextLine().trim();

        Order order = orderService.rejectOrder(orderId, restaurantId, reason, UserRole.RESTAURANT_OWNER);
        System.out.println("\n>>> Order rejected!");
        System.out.println("Order ID: " + order.getId() + " | Status: " + order.getStatus() + " | Reason: " + order.getRejectionReason());
    }

    private void handleStartPreparation() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        System.out.print("Enter Restaurant ID: ");
        String restaurantId = scanner.nextLine().trim();

        Order order = orderService.startPreparation(orderId, restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println("\n>>> Order preparation started!");
        System.out.println("Order ID: " + order.getId() + " | New Status: " + order.getStatus());
    }

    private void handleMarkReadyForPickup() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        System.out.print("Enter Restaurant ID: ");
        String restaurantId = scanner.nextLine().trim();

        Order order = orderService.markReadyForPickup(orderId, restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println("\n>>> Order ready for pickup!");
        System.out.println("Order ID: " + order.getId() + " | New Status: " + order.getStatus());
    }

    private void handleCancelOrder() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        System.out.print("Enter Customer ID: ");
        String customerId = scanner.nextLine().trim();
        System.out.print("Enter Cancellation Reason (Optional): ");
        String reason = scanner.nextLine().trim();

        Order order = orderService.cancelOrder(orderId, customerId, reason);
        System.out.println("\n>>> Order cancelled!");
        System.out.println("Order ID: " + order.getId() + " | Status: " + order.getStatus() + " | Reason: " + order.getCancellationReason());
    }

    private void displayOrderSummary(Order order) {
        System.out.println("-------------------------------------------------");
        System.out.println("Order ID        : " + order.getId());
        System.out.println("Customer        : " + order.getCustomerName() + " (" + order.getCustomerId() + ")");
        System.out.println("Restaurant      : " + order.getRestaurantName() + " (" + order.getRestaurantId() + ")");
        System.out.println("Delivery City   : " + order.getDeliveryCity());
        System.out.println("Delivery Address: " + order.getDeliveryFullAddress());
        System.out.println("Status          : " + order.getStatus());
        System.out.println("Placed At       : " + order.getPlacedAt());
        System.out.println("Items:");
        for (OrderItem item : order.getItems()) {
            System.out.println("  - " + item.getItemName() + " x" + item.getQuantity() + " @ Rs." + item.getItemPrice() + " = Rs." + item.getSubtotal());
        }
        System.out.println("Total Amount    : Rs. " + order.getTotalAmount());
        System.out.println("-------------------------------------------------");
    }
}
