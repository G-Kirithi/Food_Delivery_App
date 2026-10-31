package com.food_delivery;

import com.food_delivery.cart.model.Cart;
import com.food_delivery.cart.repository.CartRepository;
import com.food_delivery.cart.repository.InMemoryCartRepository;
import com.food_delivery.cart.service.CartService;
import com.food_delivery.cart.service.CartServiceImpl;
import com.food_delivery.cli.ordercli;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.customer.repository.InMemoryCustomerRepository;
import com.food_delivery.customer.service.CustomerService;
import com.food_delivery.customer.service.CustomerServiceImpl;
import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.model.Order;
import com.food_delivery.order.model.OrderItem;
import com.food_delivery.order.repository.InMemoryOrderRepository;
import com.food_delivery.order.repository.OrderRepository;
import com.food_delivery.order.service.ActiveOrderCheckerImpl;
import com.food_delivery.order.service.OrderService;
import com.food_delivery.order.service.OrderServiceImpl;
import com.food_delivery.payment.dto.PaymentRequest;
import com.food_delivery.payment.enums.DiscountType;
import com.food_delivery.payment.enums.PaymentMethod;
import com.food_delivery.payment.model.Coupon;
import com.food_delivery.payment.model.Payment;
import com.food_delivery.payment.repository.CouponRepository;
import com.food_delivery.payment.repository.InMemoryCouponRepository;
import com.food_delivery.payment.repository.InMemoryPaymentRepository;
import com.food_delivery.payment.repository.PaymentRepository;
import com.food_delivery.payment.service.PaymentService;
import com.food_delivery.payment.service.PaymentService.OrderTotalBreakdown;
import com.food_delivery.payment.service.PaymentServiceImpl;
import com.food_delivery.restaurant.enums.CuisineType;
import com.food_delivery.restaurant.enums.FoodCategory;
import com.food_delivery.restaurant.enums.UserRole;
import com.food_delivery.restaurant.model.MenuItem;
import com.food_delivery.restaurant.model.Restaurant;
import com.food_delivery.restaurant.repository.InMemoryMenuItemRepository;
import com.food_delivery.restaurant.repository.InMemoryRestaurantRepository;
import com.food_delivery.restaurant.repository.MenuItemRepository;
import com.food_delivery.restaurant.repository.RestaurantRepository;
import com.food_delivery.restaurant.service.MenuService;
import com.food_delivery.restaurant.service.MenuServiceImpl;
import com.food_delivery.restaurant.service.RestaurantService;
import com.food_delivery.restaurant.service.RestaurantServiceImpl;
import com.food_delivery.test.AllModulesTestSuite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Scanner;

/**
 * Main application entry point for the Food Delivery System.
 * Pre-seeds initial data and provides interactive menu options for demonstrations,
 * test suite execution, and order management CLI.
 */
public class Main {

    private static CustomerRepository customerRepo;
    private static RestaurantService restaurantService;
    private static MenuService menuService;
    private static CustomerService customerService;
    private static CartService cartService;
    private static OrderService orderService;
    private static PaymentService paymentService;

    public static void main(String[] args) {
        initDependencies();
        seedInitialData();

        System.out.println("================================================================================");
        System.out.println("      WELCOME TO THE ONLINE FOOD DELIVERY PLATFORM (MODULE 1-5)                ");
        System.out.println("================================================================================");

        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            AllModulesTestSuite.main(args);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Run Complete Test Suite (88 Tests across M1 — M5)");
            System.out.println("2. Run End-to-End Order Placement & Lifecycle Simulation Demo");
            System.out.println("3. Launch Interactive Order CLI");
            System.out.println("4. Run Module 5 Payment & Coupon Demo");
            System.out.println("5. Exit Application");
            System.out.print("Enter choice (1-5): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    System.out.println();
                    AllModulesTestSuite.main(new String[0]);
                }
                case "2" -> {
                    System.out.println();
                    runOrderSimulation();
                }
                case "3" -> {
                    System.out.println();
                    ordercli cli = new ordercli(orderService);
                    cli.start();
                }
                case "4" -> {
                    System.out.println();
                    runPaymentDemo();
                }
                case "5" -> {
                    System.out.println("\nThank you for using the Food Delivery Platform. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please select 1, 2, 3, 4, or 5.");
            }
        }
    }

    private static void initDependencies() {
        RestaurantRepository restaurantRepo = new InMemoryRestaurantRepository();
        MenuItemRepository menuRepo = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantServiceImpl(restaurantRepo, menuRepo);
        menuService = new MenuServiceImpl(menuRepo, restaurantRepo);

        customerRepo = new InMemoryCustomerRepository();
        customerService = new CustomerServiceImpl(customerRepo);

        CartRepository cartRepo = new InMemoryCartRepository();
        cartService = new CartServiceImpl(cartRepo, customerRepo, restaurantService, menuService);

        OrderRepository orderRepo = new InMemoryOrderRepository();
        orderService = new OrderServiceImpl(orderRepo, customerRepo, restaurantService, menuService, cartService);

        // Connect ActiveOrderChecker so restaurant deletion verifies active orders
        restaurantService.setActiveOrderChecker(new ActiveOrderCheckerImpl(orderRepo));

        // Module 5: Payment service
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        CouponRepository couponRepo = new InMemoryCouponRepository();
        paymentService = new PaymentServiceImpl(paymentRepo, couponRepo, orderRepo);
    }

    private static void seedInitialData() {
        // Restaurant 1 in Mumbai
        Restaurant r1 = new Restaurant(
                "REST-MUM-01", "Taj Mahal Spice", CuisineType.NORTH_INDIAN, "Mumbai",
                LocalTime.of(8, 0), LocalTime.of(23, 30), BigDecimal.valueOf(250.0)
        );
        restaurantService.addRestaurant(r1, UserRole.ADMIN);
        restaurantService.activateRestaurant("REST-MUM-01", UserRole.ADMIN);
        restaurantService.markOpen("REST-MUM-01", UserRole.ADMIN);

        menuService.addMenuItem(new MenuItem("ITEM-MUM-01", "REST-MUM-01", "Paneer Tikka Masala", BigDecimal.valueOf(280.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);
        menuService.addMenuItem(new MenuItem("ITEM-MUM-02", "REST-MUM-01", "Butter Naan", BigDecimal.valueOf(45.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);
        menuService.addMenuItem(new MenuItem("ITEM-MUM-03", "REST-MUM-01", "Dal Makhani", BigDecimal.valueOf(220.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);

        // Restaurant 2 in Delhi
        Restaurant r2 = new Restaurant(
                "REST-DEL-01", "Delhi Dawat", CuisineType.FAST_FOOD, "Delhi",
                LocalTime.of(10, 0), LocalTime.of(23, 0), BigDecimal.valueOf(150.0)
        );
        restaurantService.addRestaurant(r2, UserRole.ADMIN);
        restaurantService.activateRestaurant("REST-DEL-01", UserRole.ADMIN);
        restaurantService.markOpen("REST-DEL-01", UserRole.ADMIN);

        menuService.addMenuItem(new MenuItem("ITEM-DEL-01", "REST-DEL-01", "Crispy Veg Burger", BigDecimal.valueOf(120.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);
        menuService.addMenuItem(new MenuItem("ITEM-DEL-02", "REST-DEL-01", "Peri Peri Fries", BigDecimal.valueOf(90.0), FoodCategory.VEG, true), UserRole.RESTAURANT_OWNER);

        // Customer in Mumbai
        Address addr1 = new Address("ADDR-MUM-01", "Apt 501, Sea View Towers", "Marine Drive", "Nariman Point", "Mumbai", "Maharashtra", "400021");
        Customer c1 = new Customer("CUST-001", "Pooja Mehta", "+919820011223", "pooja.mehta@example.com", Collections.singletonList(addr1));
        customerRepo.save(c1);

        // Customer in Delhi
        Address addr2 = new Address("ADDR-DEL-01", "House 14B, Green Park", "Inner Ring Road", "South Delhi", "Delhi", "Delhi", "110016");
        Customer c2 = new Customer("CUST-002", "Vikram Singh", "+919811099887", "vikram.singh@example.com", Collections.singletonList(addr2));
        customerRepo.save(c2);
    }

    private static void runOrderSimulation() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("                 STARTING END-TO-END ORDER LIFECYCLE SIMULATION                 ");
        System.out.println("--------------------------------------------------------------------------------");

        String customerId = "CUST-001";
        String restaurantId = "REST-MUM-01";
        String addressId = "ADDR-MUM-01";

        System.out.println("[Step 1] Customer 'Pooja Mehta' browses restaurant 'Taj Mahal Spice' in Mumbai.");
        System.out.println("Adding items to cart:");
        System.out.println(" -> 1x Paneer Tikka Masala (Rs. 280.0)");
        System.out.println(" -> 2x Butter Naan (Rs. 45.0 each)");

        cartService.addItemToCart(customerId, restaurantId, "ITEM-MUM-01", 1);
        cartService.addItemToCart(customerId, restaurantId, "ITEM-MUM-02", 2);

        Cart cart = cartService.getCartByCustomerId(customerId);
        System.out.println("Cart Total: Rs. " + cart.getTotalAmount() + " (Min Order Value: Rs. 250.0 met!)");

        System.out.println("\n[Step 2] Customer places the order...");
        Order order = orderService.placeOrder(customerId, addressId, LocalTime.of(13, 0));
        System.out.println(">> Order Placed Successfully! Order ID: " + order.getId());
        System.out.println("   Status: " + order.getStatus() + " | Placed At: " + order.getPlacedAt());
        System.out.println("   Cart Cleared? " + cartService.getCartByCustomerId(customerId).isEmpty());

        System.out.println("\n[Step 3] Restaurant accepts the order (within 5 minutes)...");
        order = orderService.acceptOrder(order.getId(), restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println(">> New Status: " + order.getStatus() + " | Accepted At: " + order.getAcceptedAt());

        System.out.println("\n[Step 4] Restaurant starts preparation...");
        order = orderService.startPreparation(order.getId(), restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println(">> New Status: " + order.getStatus() + " | Preparing At: " + order.getPreparingAt());

        System.out.println("\n[Step 5] Checking cancellation rule: Customer tries to cancel after preparation started:");
        try {
            orderService.cancelOrder(order.getId(), customerId, "I want to cancel");
        } catch (Exception e) {
            System.out.println(">> [REJECTED AS EXPECTED] " + e.getMessage());
        }

        System.out.println("\n[Step 6] Food is ready! Restaurant marks order READY_FOR_PICKUP...");
        order = orderService.markReadyForPickup(order.getId(), restaurantId, UserRole.RESTAURANT_OWNER);
        System.out.println(">> New Status: " + order.getStatus() + " | Ready At: " + order.getReadyForPickupAt());

        System.out.println("\n[Step 7] Delivery partner 'DP-Ramesh' picks up order and sets OUT_FOR_DELIVERY...");
        order = orderService.markOutForDelivery(order.getId(), "DP-Ramesh");
        System.out.println(">> New Status: " + order.getStatus() + " | Partner: " + order.getDeliveryPartnerId());

        System.out.println("\n[Step 8] Order delivered to customer home...");
        order = orderService.markDelivered(order.getId());
        System.out.println(">> New Status: " + order.getStatus() + " | Delivered At: " + order.getDeliveredAt());
        System.out.println("   Is Order Active? " + order.isActive());

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("               END-TO-END SIMULATION COMPLETED SUCCESSFULLY!                    ");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void runPaymentDemo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("          MODULE 5 — PAYMENT, COUPONS & REFUNDS DEMO                           ");
        System.out.println("--------------------------------------------------------------------------------");

        String customerId = "CUST-001";
        String restaurantId = "REST-MUM-01";
        String addressId = "ADDR-MUM-01";

        // --- DEMO 1: UPI Payment with Coupon ---
        System.out.println("\n[Demo 1] UPI Payment with a Flat Coupon");
        System.out.println("Adding a Rs. 50 flat coupon 'FLAT50'...");
        Coupon coupon = new Coupon("FLAT50", DiscountType.FLAT, BigDecimal.valueOf(50),
                BigDecimal.valueOf(200), LocalDate.now().plusMonths(6));
        paymentService.addCoupon(coupon);
        System.out.println(">> Coupon added: " + coupon.getCode() + " (Rs. 50 off, min order Rs. 200)");

        cartService.addItemToCart(customerId, restaurantId, "ITEM-MUM-01", 1); // Rs. 280
        Order order1 = orderService.placeOrder(customerId, addressId, LocalTime.of(13, 0));
        System.out.println(">> Order Placed: " + order1.getId() + " | Items Total: Rs. " + order1.getTotalAmount());

        OrderTotalBreakdown breakdown = paymentService.calculateOrderTotal(
                order1.getTotalAmount(), "FLAT50", customerId);
        System.out.println(">> Order Breakdown:");
        System.out.println("   Items Total  : Rs. " + breakdown.itemsTotal);
        System.out.println("   Delivery Fee : Rs. " + breakdown.deliveryFee + " (free because items >= Rs. 500? " + breakdown.deliveryFee.equals(BigDecimal.ZERO) + ")");
        System.out.println("   5% GST       : Rs. " + breakdown.gstAmount);
        System.out.println("   Coupon Disc. : Rs. " + breakdown.couponDiscount + " (FLAT50)");
        System.out.println("   Grand Total  : Rs. " + breakdown.grandTotal);

        Payment p1 = paymentService.initiatePayment(
                new PaymentRequest(order1.getId(), customerId, PaymentMethod.UPI, "FLAT50"));
        System.out.println(">> Payment Status: " + p1.getStatus() + " | ID: " + p1.getPaymentId());

        // --- DEMO 2: COD Payment ---
        System.out.println("\n[Demo 2] COD Payment (within Rs. 1500 limit)");
        cartService.addItemToCart(customerId, restaurantId, "ITEM-MUM-02", 2); // 2 × Rs. 45 = Rs. 90
        Order order2 = orderService.placeOrder(customerId, addressId, LocalTime.of(13, 0));
        Payment p2 = paymentService.initiatePayment(
                new PaymentRequest(order2.getId(), customerId, PaymentMethod.COD));
        System.out.println(">> COD Payment Status: " + p2.getStatus() + " (PENDING until delivery)");

        // --- DEMO 3: Refund on Customer Cancellation ---
        System.out.println("\n[Demo 3] Refund issued when customer cancels online order");
        cartService.addItemToCart(customerId, restaurantId, "ITEM-MUM-03", 1); // Rs. 220
        Order order3 = orderService.placeOrder(customerId, addressId, LocalTime.of(13, 0));
        paymentService.initiatePayment(new PaymentRequest(order3.getId(), customerId, PaymentMethod.CARD));
        orderService.cancelOrder(order3.getId(), customerId, "Demo cancel");
        Payment refunded = paymentService.processRefund(order3.getId());
        System.out.println(">> Refund Status: " + refunded.getStatus() + " | Amount: Rs. " + refunded.getAmount());

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("               MODULE 5 DEMO COMPLETED SUCCESSFULLY!                            ");
        System.out.println("--------------------------------------------------------------------------------");
    }
}
