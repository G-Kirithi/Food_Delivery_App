
package com.food_delivery.delivery.model;

import java.time.LocalDateTime;

public class DeliveryAssignment {

    private String assignmentId;
    private String orderId;
    private String partnerId;
    private AssignmentStatus status;
    private LocalDateTime assignedAt;

    public DeliveryAssignment(String assignmentId, String orderId, String partnerId) {
        this.assignmentId = assignmentId;
        this.orderId = orderId;
        this.partnerId = partnerId;
        this.status = AssignmentStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
