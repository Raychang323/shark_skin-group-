package com.sharkskin.store.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore; // Add this import

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Changed to EAGER
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JsonIgnore // Add this to prevent circular reference during JSON serialization
    @ManyToOne(fetch = FetchType.LAZY) // Keep LAZY for the Cart reference
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    private int quantity;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
