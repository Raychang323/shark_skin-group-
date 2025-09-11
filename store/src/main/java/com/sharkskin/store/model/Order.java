package com.sharkskin.store.model;

import java.util.List;

public class Order {
    private String orderNumber;
    private String email;
    private String status;
    private List<OrderItem> items;
    private double totalPrice;

    public Order(String orderNumber, String email, String status, List<OrderItem> items, double totalPrice) {
        this.orderNumber = orderNumber;
        this.email = email;
        this.status = status;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
