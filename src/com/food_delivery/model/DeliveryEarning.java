
package com.food_delivery.delivery.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeliveryEarning {

    private String earningId;
    private String partnerId;
    private String orderId;

    private BigDecimal amount;
    private LocalDateTime earnedAt;

    public DeliveryEarning(String earningId,
                           String partnerId,
                           String orderId) {

        this.earningId = earningId;
        this.partnerId = partnerId;
        this.orderId = orderId;

        this.amount = BigDecimal.valueOf(30);
        this.earnedAt = LocalDateTime.now();
    }

    public String getEarningId() {
        return earningId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
}
