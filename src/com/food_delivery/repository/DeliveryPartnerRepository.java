
package com.food_delivery.delivery.repository;

import com.food_delivery.delivery.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryPartnerRepository {

    private final ArrayList<DeliveryPartner> partners =
            new ArrayList<>();

    private final ArrayList<DeliveryAssignment> assignments =
            new ArrayList<>();

    private final ArrayList<DeliveryEarning> earnings =
            new ArrayList<>();

    // Save partner
    public void savePartner(DeliveryPartner partner) {
        partners.add(partner);
    }

    // Find partner by ID
    public Optional<DeliveryPartner> findPartnerById(String id) {
        return partners.stream()
                .filter(p -> p.getPartnerId().equals(id))
                .findFirst();
    }

    // Get all partners
    public List<DeliveryPartner> getAllPartners() {
        return new ArrayList<>(partners);
    }

    // Save assignment
    public void saveAssignment(DeliveryAssignment assignment) {
        assignments.add(assignment);
    }

    // Get all assignments
    public List<DeliveryAssignment> getAllAssignments() {
        return new ArrayList<>(assignments);
    }

    // Save earning
    public void saveEarning(DeliveryEarning earning) {
        earnings.add(earning);
    }

    // Get earnings of a partner
    public List<DeliveryEarning> getEarningsByPartner(String partnerId) {
        ArrayList<DeliveryEarning> result = new ArrayList<>();

        for (DeliveryEarning earning : earnings) {
            if (earning.getPartnerId().equals(partnerId)) {
                result.add(earning);
            }
        }

        return result;
    }

    // Get all earnings
    public List<DeliveryEarning> getAllEarnings() {
        return new ArrayList<>(earnings);
    }
}
