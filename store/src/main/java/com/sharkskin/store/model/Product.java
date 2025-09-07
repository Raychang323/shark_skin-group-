package com.sharkskin.store.model;

public class Product {
    private String p_id;
    private String name;
    private int price;
    private String pic_url;

    public Product(String p_id, String name, int price, String pic_url) {
        this.p_id = p_id;
        this.name = name;
        this.price = price;
        this.pic_url = pic_url;
    }

    // Getters and Setters
    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }
}
