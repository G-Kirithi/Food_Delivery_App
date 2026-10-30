
package com.food_delivery.delivery.service;

import com.food_delivery.delivery.model.*;
import java.util.List;

public interface DeliveryPartnerService {

    // Register a delivery partner
    DeliveryPartner registerPartner(DeliveryPartner partner);

    // View all partners
    List<DeliveryPartner> getAllPartners();

    // Update partner status
    void updatePartnerStatus(String partnerId, PartnerStatus status);

    // Assign partner to a ready-for-pickup order
    DeliveryAssignment assignPartnerForReadyOrder(String orderId);

    // Accept an assignment
    void acceptAssignment(String assignmentId);

    // Reject an assignment
    void rejectAssignment(String assignmentId);

    // Start delivery
    void markOutForDelivery(String orderId, String partnerId);

    // Complete delivery
    void completeDelivery(String orderId, String partnerId);

    // View waiting orders
    List<String> getWaitingOrderIds();

    // View all assignments
    List<DeliveryAssignment> getAllAssignments();

    // View partner earnings
    List<DeliveryEarning> getPartnerEarnings(String partnerId);
}

