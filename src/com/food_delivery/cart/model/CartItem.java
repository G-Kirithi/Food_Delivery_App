package com.food_delivery.cart.model;

import com.food_delivery.cart.exception.InvalidQuantityException;
import com.food_delivery.restaurant.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Model representing an item line in a Customer's Cart.
 */
public class CartItem {
    public static final int MAX_ITEM_QUANTITY = 10;
    public static final int MIN_ITEM_QUANTITY = 1;

    private final String id;
    private final String menuItemId;
    private final String itemName;
    private final BigDecimal itemPrice;
    private int quantity;
    private BigDecimal subtotal;

    public CartItem(String id,
                    String menuItemId,
                    String itemName,
                    BigDecimal itemPrice,
                    int quantity) {
        validateId(id);
        validateMenuItemId(menuItemId);
        validateItemName(itemName);
        validatePrice(itemPrice);
        validateQuantity(quantity);

        this.id = id.trim();
        this.menuItemId = menuItemId.trim();
        this.itemName = itemName.trim();
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.subtotal = calculateSubtotal(itemPrice, quantity);
    }

    public String getId() {
        return id;
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

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
        this.subtotal = calculateSubtotal(this.itemPrice, quantity);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    private static BigDecimal calculateSubtotal(BigDecimal price, int qty) {
        return price.multiply(BigDecimal.valueOf(qty));
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Cart item ID cannot be null or blank.");
        }
    }

    private void validateMenuItemId(String menuItemId) {
        if (menuItemId == null || menuItemId.trim().isEmpty()) {
            throw new ValidationException("Menu item ID cannot be null or blank.");
        }
    }

    private void validateItemName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new ValidationException("Item name cannot be null or blank.");
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
        if (quantity < MIN_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Quantity must be greater than zero. Provided: " + quantity);
        }
        if (quantity > MAX_ITEM_QUANTITY) {
            throw new InvalidQuantityException("Maximum " + MAX_ITEM_QUANTITY + " units of the same menu item can be added. Provided: " + quantity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id='" + id + '\'' +
                ", menuItemId='" + menuItemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
