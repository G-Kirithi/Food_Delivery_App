package com.food_delivery.order.model;

import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable snapshot of an ordered menu item line.
 * Stores the price at the time of ordering so that subsequent restaurant menu
 * price changes do not alter existing orders.
 */
public class OrderItem {
    private final String menuItemId;
    private final String itemName;
    private final BigDecimal itemPrice;
    private final int quantity;
    private final BigDecimal subtotal;

    public OrderItem(String menuItemId, String itemName, BigDecimal itemPrice, int quantity) {
        validateMenuItemId(menuItemId);
        validateItemName(itemName);
        validatePrice(itemPrice);
        validateQuantity(quantity);

        this.menuItemId = menuItemId.trim();
        this.itemName = itemName.trim();
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.subtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    private void validateMenuItemId(String menuItemId) {
        if (menuItemId == null || menuItemId.trim().isEmpty()) {
            throw new ValidationException("Menu item ID cannot be null or blank in order item.");
        }
    }

    private void validateItemName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new ValidationException("Item name cannot be null or blank in order item.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Item price cannot be null.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Item price must be greater than zero. Provided: " + price);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero. Provided: " + quantity);
        }
        if (quantity > 10) {
            throw new ValidationException("Maximum 10 units allowed per item. Provided: " + quantity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
                Objects.equals(menuItemId, orderItem.menuItemId) &&
                Objects.equals(itemName, orderItem.itemName) &&
                Objects.equals(itemPrice, orderItem.itemPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItemId, itemName, itemPrice, quantity);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "menuItemId='" + menuItemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
