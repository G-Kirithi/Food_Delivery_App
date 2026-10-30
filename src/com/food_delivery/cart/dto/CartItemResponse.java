package com.food_delivery.cart.dto;

import com.food_delivery.cart.model.CartItem;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing an individual Cart Item in API responses.
 */
public class CartItemResponse {
    private String id;
    private String menuItemId;
    private String itemName;
    private BigDecimal itemPrice;
    private int quantity;
    private BigDecimal subtotal;

    public CartItemResponse() {
    }

    public CartItemResponse(CartItem item) {
        if (item != null) {
            this.id = item.getId();
            this.menuItemId = item.getMenuItemId();
            this.itemName = item.getItemName();
            this.itemPrice = item.getItemPrice();
            this.quantity = item.getQuantity();
            this.subtotal = item.getSubtotal();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
