package com.food_delivery.customer.test;

import com.food_delivery.common.api.ApiResponse;
import com.food_delivery.customer.controller.CustomerController;
import com.food_delivery.customer.dto.AddressRequest;
import com.food_delivery.customer.dto.CustomerRegistrationRequest;
import com.food_delivery.customer.exception.CustomerNotFoundException;
import com.food_delivery.customer.exception.CustomerValidationException;
import com.food_delivery.customer.exception.DuplicatePhoneNumberException;
import com.food_delivery.customer.exception.InvalidAddressException;
import com.food_delivery.customer.model.Address;
import com.food_delivery.customer.model.Customer;
import com.food_delivery.customer.repository.CustomerRepository;
import com.food_delivery.customer.repository.InMemoryCustomerRepository;
import com.food_delivery.customer.service.CustomerService;
import com.food_delivery.customer.service.CustomerServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic Unit Test Suite for Customer and Address functionality.
 */
public class CustomerTestSuite {

    private CustomerRepository customerRepo;
    private CustomerService customerService;
    private CustomerController customerController;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("         RUNNING FOOD DELIVERY APP - CUSTOMER TEST SUITE (11 TEST CASES)        ");
        System.out.println("================================================================================");

        CustomerTestSuite suite = new CustomerTestSuite();
        int passed = 0;
        int failed = 0;

        try {
            suite.setUp(); suite.test1_registerCustomerWithValidDetails_Success(); passed++; System.out.println("[PASS] Customer Test 1: Register customer with valid details — SUCCESS");
            suite.setUp(); suite.test2_registerCustomerWithDuplicatePhone_Failure(); passed++; System.out.println("[PASS] Customer Test 2: Register customer with duplicate phone number — FAILURE");
            suite.setUp(); suite.test3_registerCustomerWithInvalidEmail_Failure(); passed++; System.out.println("[PASS] Customer Test 3: Register customer with invalid email — FAILURE");
            suite.setUp(); suite.test4_registerCustomerWithoutAddress_Failure(); passed++; System.out.println("[PASS] Customer Test 4: Register customer without address — FAILURE");
            suite.setUp(); suite.test5_addMultipleAddresses_Success(); passed++; System.out.println("[PASS] Customer Test 5: Add multiple addresses — SUCCESS");
            suite.setUp(); suite.test6_deleteAddressWhenAnotherExists_Success(); passed++; System.out.println("[PASS] Customer Test 6: Delete an address when another address exists — SUCCESS");
            suite.setUp(); suite.test7_deleteLastRequiredAddress_Failure(); passed++; System.out.println("[PASS] Customer Test 7: Delete the last required address — FAILURE");
            suite.setUp(); suite.test8_registerCustomerWithEmptyName_Failure(); passed++; System.out.println("[PASS] Customer Test 8: Register customer with empty name — FAILURE");
            suite.setUp(); suite.test9_registerCustomerWithInvalidPhoneFormat_Failure(); passed++; System.out.println("[PASS] Customer Test 9: Register customer with invalid phone format — FAILURE");
            suite.setUp(); suite.test10_updateCustomerProfileAndAddress_Success(); passed++; System.out.println("[PASS] Customer Test 10: Update customer profile and address — SUCCESS");
            suite.setUp(); suite.test11_customerControllerEndpoints_Success(); passed++; System.out.println("[PASS] Customer Test 11: Customer controller REST API endpoints — SUCCESS");
        } catch (Throwable t) {
            failed++;
            System.err.println("\n[FAIL] Customer test execution encountered failure:");
            t.printStackTrace();
        }

        System.out.println("================================================================================");
        System.out.println("CUSTOMER TESTS: " + (passed + failed) + " | PASSED: " + passed + " | FAILED: " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    public void setUp() {
        customerRepo = new InMemoryCustomerRepository();
        customerService = new CustomerServiceImpl(customerRepo);
        customerController = new CustomerController(customerService);
    }

    public void test1_registerCustomerWithValidDetails_Success() {
        AddressRequest address = new AddressRequest(
                "Flat 101, Lotus Apts", "Gandhi Road", "RS Puram", "Coimbatore", "Tamil Nadu", "641002", "Near Post Office"
        );
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "CUST-001", "Test Customer", "9876543210", "test@example.com", address
        );

        Customer registered = customerService.registerCustomer(request);
        assertTrue(registered != null, "Registered customer should not be null");
        assertEquals("CUST-001", registered.getId(), "Customer ID matches");
        assertEquals("Test Customer", registered.getFullName(), "Customer name matches");
        assertEquals("9876543210", registered.getPhoneNumber(), "Phone number matches");
        assertEquals("test@example.com", registered.getEmail(), "Email matches");
        assertEquals(1, registered.getAddresses().size(), "Customer has 1 address");
        assertEquals("Coimbatore", registered.getAddresses().get(0).getCity(), "Address city matches");
    }

    public void test2_registerCustomerWithDuplicatePhone_Failure() {
        AddressRequest address1 = new AddressRequest("Apt 1", "Street 1", "Area 1", "Coimbatore", "Tamil Nadu", "641001", null);
        CustomerRegistrationRequest req1 = new CustomerRegistrationRequest("CUST-001", "First Customer", "9876543210", "first@example.com", address1);
        customerService.registerCustomer(req1);

        AddressRequest address2 = new AddressRequest("Apt 2", "Street 2", "Area 2", "Coimbatore", "Tamil Nadu", "641001", null);
        CustomerRegistrationRequest req2 = new CustomerRegistrationRequest("CUST-002", "Second Customer", "9876543210", "second@example.com", address2);

        assertThrows(DuplicatePhoneNumberException.class, () -> customerService.registerCustomer(req2),
                "Should reject registration with duplicate phone number");
    }

    public void test3_registerCustomerWithInvalidEmail_Failure() {
        AddressRequest address = new AddressRequest("Apt 1", "Street 1", "Area 1", "Coimbatore", "Tamil Nadu", "641001", null);
        CustomerRegistrationRequest req = new CustomerRegistrationRequest("CUST-003", "Invalid Email Customer", "9876543211", "notanemail", address);

        assertThrows(CustomerValidationException.class, () -> customerService.registerCustomer(req),
                "Should reject customer with invalid email");
    }

    public void test4_registerCustomerWithoutAddress_Failure() {
        // Null address list
        CustomerRegistrationRequest req1 = new CustomerRegistrationRequest("CUST-004", "No Address Customer", "9876543212", "test4@example.com", (List<AddressRequest>) null);
        assertThrows(InvalidAddressException.class, () -> customerService.registerCustomer(req1),
                "Should reject customer without addresses (null list)");

        // Empty address list
        CustomerRegistrationRequest req2 = new CustomerRegistrationRequest("CUST-005", "Empty Address Customer", "9876543213", "test5@example.com", Collections.emptyList());
        assertThrows(InvalidAddressException.class, () -> customerService.registerCustomer(req2),
                "Should reject customer without addresses (empty list)");
    }

    public void test5_addMultipleAddresses_Success() {
        AddressRequest primary = new AddressRequest("ADDR-1", "House 1", "Cross 1", "Peelamedu", "Coimbatore", "Tamil Nadu", "641004", "Near Airport");
        CustomerRegistrationRequest req = new CustomerRegistrationRequest("CUST-006", "Multi Addr Customer", "9876543214", "multi@example.com", primary);
        customerService.registerCustomer(req);

        AddressRequest secondary = new AddressRequest("ADDR-2", "Office 2B", "Avinashi Road", "Hope College", "Coimbatore", "Tamil Nadu", "641014", "Tech Park");
        Address added = customerService.addAddress("CUST-006", secondary);

        assertTrue(added != null, "Added address must not be null");
        assertEquals("ADDR-2", added.getId(), "Address ID matches");

        List<Address> addresses = customerService.getAddresses("CUST-006");
        assertEquals(2, addresses.size(), "Customer now has 2 addresses");
    }

    public void test6_deleteAddressWhenAnotherExists_Success() {
        List<AddressRequest> addrList = new ArrayList<>();
        addrList.add(new AddressRequest("ADDR-A", "House A", "Street A", "Area A", "Coimbatore", "Tamil Nadu", "641001", null));
        addrList.add(new AddressRequest("ADDR-B", "House B", "Street B", "Area B", "Coimbatore", "Tamil Nadu", "641002", null));

        CustomerRegistrationRequest req = new CustomerRegistrationRequest("CUST-007", "Delete Addr Customer", "9876543215", "delete@example.com", addrList);
        customerService.registerCustomer(req);

        // Deleting ADDR-B when ADDR-A still exists should succeed
        customerService.deleteAddress("CUST-007", "ADDR-B");

        List<Address> remaining = customerService.getAddresses("CUST-007");
        assertEquals(1, remaining.size(), "1 address remains after deletion");
        assertEquals("ADDR-A", remaining.get(0).getId(), "Remaining address is ADDR-A");
    }

    public void test7_deleteLastRequiredAddress_Failure() {
        AddressRequest singleAddr = new AddressRequest("ADDR-LAST", "House Only", "Street 1", "Area 1", "Coimbatore", "Tamil Nadu", "641001", null);
        CustomerRegistrationRequest req = new CustomerRegistrationRequest("CUST-008", "Single Addr Customer", "9876543216", "single@example.com", singleAddr);
        customerService.registerCustomer(req);

        // Deleting the only remaining address must fail
        assertThrows(InvalidAddressException.class, () -> customerService.deleteAddress("CUST-008", "ADDR-LAST"),
                "Should reject deletion of the last remaining delivery address");
    }

    public void test8_registerCustomerWithEmptyName_Failure() {
        AddressRequest addr = new AddressRequest("House 1", "Street 1", "Area 1", "Coimbatore", "Tamil Nadu", "641001", null);

        // Blank name
        CustomerRegistrationRequest req1 = new CustomerRegistrationRequest("CUST-009", "   ", "9876543217", "blank@example.com", addr);
        assertThrows(CustomerValidationException.class, () -> customerService.registerCustomer(req1),
                "Should reject blank customer name");

        // Null name
        CustomerRegistrationRequest req2 = new CustomerRegistrationRequest("CUST-010", null, "9876543218", "null@example.com", addr);
        assertThrows(CustomerValidationException.class, () -> customerService.registerCustomer(req2),
                "Should reject null customer name");
    }

    public void test9_registerCustomerWithInvalidPhoneFormat_Failure() {
        AddressRequest addr = new AddressRequest("House 1", "Street 1", "Area 1", "Coimbatore", "Tamil Nadu", "641001", null);

        // Too short
        CustomerRegistrationRequest req1 = new CustomerRegistrationRequest("CUST-011", "Short Phone", "12345", "test@example.com", addr);
        assertThrows(CustomerValidationException.class, () -> customerService.registerCustomer(req1),
                "Should reject short phone number");

        // Contains letters
        CustomerRegistrationRequest req2 = new CustomerRegistrationRequest("CUST-012", "Alpha Phone", "98765abcde", "test@example.com", addr);
        assertThrows(CustomerValidationException.class, () -> customerService.registerCustomer(req2),
                "Should reject phone with alphabets");
    }

    public void test10_updateCustomerProfileAndAddress_Success() {
        AddressRequest addr = new AddressRequest("ADDR-U1", "House U1", "Street U1", "Area U1", "Coimbatore", "Tamil Nadu", "641001", null);
        CustomerRegistrationRequest req = new CustomerRegistrationRequest("CUST-013", "Old Name", "9876543219", "old@example.com", addr);
        customerService.registerCustomer(req);

        // Update profile
        Customer updated = customerService.updateCustomer("CUST-013", "Updated Name", "updated@example.com");
        assertEquals("Updated Name", updated.getFullName(), "Customer name updated");
        assertEquals("updated@example.com", updated.getEmail(), "Customer email updated");

        // Update address
        AddressRequest updateAddrReq = new AddressRequest("House U1-New", "Street U1-New", "Area U1-New", "Coimbatore", "Tamil Nadu", "641002", "New Landmark");
        Address updatedAddr = customerService.updateAddress("CUST-013", "ADDR-U1", updateAddrReq);
        assertEquals("House U1-New", updatedAddr.getHouseDetails(), "Address house details updated");
        assertEquals("641002", updatedAddr.getPincode(), "Pincode updated");
        assertEquals("New Landmark", updatedAddr.getLandmark(), "Landmark updated");
    }

    public void test11_customerControllerEndpoints_Success() {
        AddressRequest addr = new AddressRequest("Villa 1", "Cross 2", "Saibaba Colony", "Coimbatore", "Tamil Nadu", "641011", null);
        CustomerRegistrationRequest regReq = new CustomerRegistrationRequest("CUST-API", "API Customer", "9876543220", "api@example.com", addr);

        ApiResponse<Customer> regResp = customerController.registerCustomer(regReq);
        assertEquals(201, regResp.getStatusCode(), "Controller register should return 201 Created");
        assertTrue(regResp.isSuccess(), "Response should be success");

        ApiResponse<Customer> getResp = customerController.getCustomer("CUST-API");
        assertEquals(200, getResp.getStatusCode(), "Controller get customer returns 200 OK");
        assertEquals("API Customer", getResp.getData().getFullName(), "Retrieved name matches");

        AddressRequest newAddr = new AddressRequest("ADDR-API2", "Villa 2", "Cross 3", "Saibaba Colony", "Coimbatore", "Tamil Nadu", "641011", null);
        ApiResponse<Address> addAddrResp = customerController.addAddress("CUST-API", newAddr);
        assertEquals(201, addAddrResp.getStatusCode(), "Controller add address returns 201 Created");

        ApiResponse<List<Address>> listAddrResp = customerController.getAddresses("CUST-API");
        assertEquals(2, listAddrResp.getData().size(), "Controller returns 2 addresses");

        ApiResponse<Void> delAddrResp = customerController.deleteAddress("CUST-API", "ADDR-API2");
        assertEquals(200, delAddrResp.getStatusCode(), "Controller delete address returns 200 OK");
    }

    // --- Assertion Utilities ---
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Assertion Failed: " + message + " [Expected: " + expected + ", Actual: " + actual + "]");
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedException, Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but none thrown. " + message);
        } catch (Throwable t) {
            if (!expectedException.isInstance(t)) {
                throw new AssertionError("Assertion Failed: Expected exception " + expectedException.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage() + ". " + message, t);
            }
        }
    }
}
