package com.sharkskin.store.dto;

import java.util.List;

public class CartSummaryDto {
    private List<CartItemDto> items;
    private double totalPrice;

    public CartSummaryDto(List<CartItemDto> items, double totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}