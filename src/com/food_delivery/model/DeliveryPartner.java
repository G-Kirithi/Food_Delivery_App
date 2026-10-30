
package com.food_delivery.delivery.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DeliveryPartner {

    private String partnerId;
    private String name;
    private String phoneNumber;
    private String city;

    private PartnerStatus status;
    private String currentOrderId;

    private int dailyRejectionCount;
    private LocalDate rejectionDate;

    private BigDecimal totalEarnings;

    public DeliveryPartner(String partnerId, String name,
                           String phoneNumber, String city) {

        if (partnerId == null || partnerId.isBlank()
                || name == null || name.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()
                || city == null || city.isBlank()) {
            throw new IllegalArgumentException(
                    "Partner details cannot be empty.");
        }

        this.partnerId = partnerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.city = city;

        this.status = PartnerStatus.AVAILABLE;
        this.currentOrderId = null;
        this.dailyRejectionCount = 0;
        this.rejectionDate = LocalDate.now();
        this.totalEarnings = BigDecimal.ZERO;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public PartnerStatus getStatus() {
        return status;
    }

    public String getCurrentOrderId() {
        return currentOrderId;
    }

    public int getDailyRejectionCount() {
        resetRejectionCountIfNewDay();
        return dailyRejectionCount;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setStatus(PartnerStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    public void assignOrder(String orderId) {
        if (status != PartnerStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Partner is not available.");
        }

        this.currentOrderId = orderId;
        this.status = PartnerStatus.BUSY;
    }

    public void releaseOrder() {
        this.currentOrderId = null;
        this.status = PartnerStatus.AVAILABLE;
    }

    public void recordRejection() {
        resetRejectionCountIfNewDay();
        dailyRejectionCount++;
    }

    private void resetRejectionCountIfNewDay() {
        LocalDate today = LocalDate.now();

        if (!today.equals(rejectionDate)) {
            dailyRejectionCount = 0;
            rejectionDate = today;
        }
    }

    public void addEarnings(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Earning must be greater than zero.");
        }

        totalEarnings = totalEarnings.add(amount);
    }

    @Override
    public String toString() {
        return "DeliveryPartner{" +
                "partnerId='" + partnerId + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", status=" + status +
                ", currentOrderId='" + currentOrderId + '\'' +
                ", totalEarnings=" + totalEarnings +
                '}';
    }
}
