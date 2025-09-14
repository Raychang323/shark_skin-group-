package com.sharkskin.store.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" is a reserved SQL keyword
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private String email; // To link with the user

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY) // Add this annotation
    @JoinColumn(name = "user_id") // This will create a user_id column in the orders table
    private UserModel user; // Add this field

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // JPA requires a no-arg constructor
    public Order() {}

    public Order(String orderNumber, String email, OrderStatus status, PaymentMethod paymentMethod, double totalPrice) {
        this.orderNumber = orderNumber;
        this.email = email;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public UserModel getUser() { // Add this getter
        return user;
    }

    public void setUser(UserModel user) { // Add this setter
        this.user = user;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getDisplayStatus() {
        return this.status != null ? this.status.getDisplayName() : "";
    }

    public String getDisplayPaymentMethod() {
        return this.paymentMethod != null ? this.paymentMethod.getDisplayName() : "";
    }

    // Helper methods for bidirectional relationship
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
