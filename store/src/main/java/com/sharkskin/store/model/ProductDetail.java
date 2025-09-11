package com.sharkskin.store.model;

import java.util.List;

public class ProductDetail {

    private String p_id;
    private String p_name;
    private int price;
    private List<String> pic_urls;
    private int on_stock;

    public ProductDetail(String p_id, String p_name, int price, List<String> pic_urls, int on_stock) {
        this.p_id = p_id;
        this.p_name = p_name;
        this.price = price;
        this.pic_urls = pic_urls;
        this.on_stock = on_stock;
    }

    // Getters and Setters
    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }
    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<String> getPic_urls() {
        return pic_urls;
    }

    public void setPic_urls(List<String> pic_urls) {
        this.pic_urls = pic_urls;
    }

    public int getOn_stock() {
        return on_stock;
    }

    public void setOn_stock(int on_stock) {
        this.on_stock = on_stock;
    }
}
