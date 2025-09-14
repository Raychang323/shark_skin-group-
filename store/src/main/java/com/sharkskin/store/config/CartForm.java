package com.sharkskin.store.config;

import java.util.ArrayList;
import java.util.List;

public class CartForm {
    private List<CartItemForm> items = new ArrayList<>(); // 🚀 預設不為 null

    public List<CartItemForm> getItems() {
        return items;
    }

    public void setItems(List<CartItemForm> items) {
        this.items = items;
    }
}
