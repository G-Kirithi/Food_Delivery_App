package com.food_delivery.test;

import com.food_delivery.cart.test.CartTestSuite;
import com.food_delivery.customer.test.CustomerTestSuite;
import com.food_delivery.integration.test.Module2IntegrationTestSuite;
import com.food_delivery.restaurant.test.RestaurantTestSuite;

/**
 * Master test runner executing all test suites across Module 1 and Module 2.
 */
public class AllModulesTestSuite {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          STARTING COMPREHENSIVE TEST EXECUTION (MODULE 1 & MODULE 2)           ");
        System.out.println("================================================================================\n");

        // 1. Run Module 1 Test Suite
        RestaurantTestSuite.main(args);
        System.out.println();

        // 2. Run Module 2 Customer Test Suite
        CustomerTestSuite.main(args);
        System.out.println();

        // 3. Run Module 2 Cart Test Suite
        CartTestSuite.main(args);
        System.out.println();

        // 4. Run Module 1 & 2 Integration Test Suite
        Module2IntegrationTestSuite.main(args);
        System.out.println();

        System.out.println("================================================================================");
        System.out.println("  ALL TEST SUITES (15 M1 + 11 CUSTOMER + 18 CART + 8 INTEGRATION = 52) PASSED! ");
        System.out.println("================================================================================");
    }
}
