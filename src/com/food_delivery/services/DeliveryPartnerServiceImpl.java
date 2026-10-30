
package com.food_delivery.delivery.service;

import com.food_delivery.delivery.model.*;
import com.food_delivery.delivery.repository.DeliveryPartnerRepository;


import com.food_delivery.order.model.Order;
import com.food_delivery.order.enums.OrderStatus;
import com.food_delivery.order.repository.OrderRepository;

import com.food_delivery.restaurant.model.Restaurant;
import com.food_delivery.restaurant.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    // ArrayList used as FIFO waiting queue
    private final ArrayList<String> waitingQueue = new ArrayList<>();

    public DeliveryPartnerServiceImpl(
            DeliveryPartnerRepository partnerRepository,
            OrderRepository orderRepository,
            RestaurantRepository restaurantRepository) {

        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
    }

    // 1. Register partner
    @Override
    public DeliveryPartner registerPartner(DeliveryPartner partner) {

        if (partner == null) {
            throw new IllegalArgumentException(
                    "Delivery partner cannot be null.");
        }

        for (DeliveryPartner existing : partnerRepository.getAllPartners()) {

            if (existing.getPartnerId().equals(partner.getPartnerId())) {
                throw new IllegalArgumentException(
                        "Partner ID already exists.");
            }

            if (existing.getPhoneNumber().equals(partner.getPhoneNumber())) {
                throw new IllegalArgumentException(
                        "Phone number already exists.");
            }
        }

        partnerRepository.savePartner(partner);

        // Check whether waiting orders can now be assigned
        assignWaitingOrders(null);

        return partner;
    }

    // 2. View all partners
    @Override
    public List<DeliveryPartner> getAllPartners() {
        return partnerRepository.getAllPartners();
    }

    // 3. Update partner status
    @Override
    public void updatePartnerStatus(
            String partnerId, PartnerStatus status) {

        DeliveryPartner partner = getPartner(partnerId);

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }

        if (partner.getCurrentOrderId() != null) {
            throw new IllegalStateException(
                    "Partner has an active order.");
        }

        if (status == PartnerStatus.BUSY) {
            throw new IllegalArgumentException(
                    "BUSY status is managed automatically during assignment.");
        }

        partner.setStatus(status);

        if (status == PartnerStatus.AVAILABLE) {
            assignWaitingOrders(null);
        }
    }

    // 4. Assign partner to order
    @Override
    public DeliveryAssignment assignPartnerForReadyOrder(String orderId) {

        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException(
                    "Order must be READY_FOR_PICKUP.");
        }

        if (order.getDeliveryPartnerId() != null) {
            throw new IllegalStateException(
                    "Order already has a delivery partner.");
        }

        if (waitingQueue.contains(orderId)) {
            throw new IllegalStateException(
                    "Order is already waiting for a partner.");
        }

        // Add at the end to maintain FIFO
        waitingQueue.add(orderId);

        assignWaitingOrders(null);

        return findAssignmentByOrderId(orderId);
    }

    // 5. Accept assignment
    @Override
    public void acceptAssignment(String assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Assignment cannot be accepted.");
        }

        assignment.setStatus(AssignmentStatus.ACCEPTED);
    }

    // 6. Reject assignment
    @Override
    public void rejectAssignment(String assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Only an assigned partner can reject.");
        }

        DeliveryPartner partner =
                getPartner(assignment.getPartnerId());

        if (partner.getDailyRejectionCount() >= 3) {
            throw new IllegalStateException(
                    "Daily rejection limit of 3 reached.");
        }

        Order order = getOrder(assignment.getOrderId());

        partner.recordRejection();

        // Release the rejected partner
        partner.releaseOrder();

        assignment.setStatus(AssignmentStatus.REJECTED);

        order.setDeliveryPartnerId(null);
        orderRepository.save(order);

        // Give this order priority because it was already waiting
        if (!waitingQueue.contains(order.getId())) {
            waitingQueue.add(0, order.getId());
        }

        // Do not immediately assign the same order to
        // the partner who just rejected it
        assignWaitingOrders(partner.getPartnerId());
    }

    // 7. Start delivery
    @Override
    public void markOutForDelivery(
            String orderId, String partnerId) {

        Order order = getOrder(orderId);
        DeliveryPartner partner = getPartner(partnerId);

        if (!partnerId.equals(order.getDeliveryPartnerId())
                || !orderId.equals(partner.getCurrentOrderId())) {
            throw new IllegalStateException(
                    "This partner is not assigned to this order.");
        }

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException(
                    "Order is not ready for pickup.");
        }

        DeliveryAssignment assignment =
                findAssignmentByOrderId(orderId);

        if (assignment == null
                || assignment.getStatus() != AssignmentStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Partner must accept the assignment first.");
        }

        order.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);
    }

    // 8. Complete delivery
    @Override
    public void completeDelivery(
            String orderId, String partnerId) {

        Order order = getOrder(orderId);
        DeliveryPartner partner = getPartner(partnerId);

        if (!partnerId.equals(order.getDeliveryPartnerId())
                || !orderId.equals(partner.getCurrentOrderId())) {
            throw new IllegalStateException(
                    "Partner is not assigned to this order.");
        }

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "Order must be OUT_FOR_DELIVERY.");
        }

        // Update order status
        order.transitionTo(OrderStatus.DELIVERED);
        orderRepository.save(order);

        // Add fixed Rs. 30 earnings
        DeliveryEarning earning = new DeliveryEarning(
                "ERN-" + (partnerRepository.getAllEarnings().size() + 1),
                partnerId,
                orderId
        );

        partner.addEarnings(BigDecimal.valueOf(30));
        partnerRepository.saveEarning(earning);

        // Mark assignment completed
        DeliveryAssignment assignment =
                findAssignmentByOrderId(orderId);

        if (assignment != null) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
        }

        // Partner becomes available again
        partner.releaseOrder();

        // Assign next waiting order if available
        assignWaitingOrders(null);
    }

    // 9. View waiting queue
    @Override
    public List<String> getWaitingOrderIds() {
        return new ArrayList<>(waitingQueue);
    }

    // 10. View assignments
    @Override
    public List<DeliveryAssignment> getAllAssignments() {
        return partnerRepository.getAllAssignments();
    }

    // 11. View partner earnings
    @Override
    public List<DeliveryEarning> getPartnerEarnings(String partnerId) {

        getPartner(partnerId);

        return partnerRepository.getEarningsByPartner(partnerId);
    }

    // ---------------------------------
    // Helper methods
    // ---------------------------------

    private DeliveryPartner getPartner(String partnerId) {

        return partnerRepository.findPartnerById(partnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Delivery partner not found."));
    }

    private Order getOrder(String orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found."));
    }

    private DeliveryAssignment getAssignment(String assignmentId) {

        for (DeliveryAssignment assignment :
                partnerRepository.getAllAssignments()) {

            if (assignment.getAssignmentId().equals(assignmentId)) {
                return assignment;
            }
        }

        throw new IllegalArgumentException(
                "Assignment not found.");
    }

    private DeliveryAssignment findAssignmentByOrderId(String orderId) {

        for (DeliveryAssignment assignment :
                partnerRepository.getAllAssignments()) {

            if (assignment.getOrderId().equals(orderId)
                    && (assignment.getStatus() == AssignmentStatus.ASSIGNED
                    || assignment.getStatus() == AssignmentStatus.ACCEPTED
                    || assignment.getStatus() == AssignmentStatus.COMPLETED)) {

                return assignment;
            }
        }

        return null;
    }

    // ---------------------------------
    // FIFO Assignment Logic
    // ---------------------------------

    private void assignWaitingOrders(String excludedPartnerId) {

        for (DeliveryPartner partner :
                partnerRepository.getAllPartners()) {

            if (partner.getStatus() != PartnerStatus.AVAILABLE) {
                continue;
            }

            if (excludedPartnerId != null
                    && excludedPartnerId.equals(partner.getPartnerId())) {
                continue;
            }

            assignOldestWaitingOrder(partner);
        }
    }

    private void assignOldestWaitingOrder(DeliveryPartner partner) {

        int index = 0;

        while (index < waitingQueue.size()) {

            String orderId = waitingQueue.get(index);

            Order order;

            try {
                order = getOrder(orderId);
            } catch (IllegalArgumentException exception) {
                waitingQueue.remove(index);
                continue;
            }

            // Remove orders that are no longer ready
            if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
                waitingQueue.remove(index);
                continue;
            }

            Restaurant restaurant = restaurantRepository
                    .findById(order.getRestaurantId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Restaurant not found."));

            // Partner and restaurant must belong to same city
            if (!partner.getCity().equalsIgnoreCase(
                    restaurant.getCity())) {

                index++;
                continue;
            }

            // Remove order from waiting queue
            waitingQueue.remove(index);

            // Assign order
            partner.assignOrder(orderId);
            order.setDeliveryPartnerId(partner.getPartnerId());

            orderRepository.save(order);

            DeliveryAssignment assignment =
                    new DeliveryAssignment(
                            "ASG-" +
                            (partnerRepository.getAllAssignments().size() + 1),
                            orderId,
                            partner.getPartnerId()
                    );

            partnerRepository.saveAssignment(assignment);

            // One partner can carry only one order
            break;
        }
    }
}
