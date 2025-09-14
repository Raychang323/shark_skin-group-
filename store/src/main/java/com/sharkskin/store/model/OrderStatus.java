package com.sharkskin.store.model;

public enum OrderStatus {
    PROCESSING("處理中"),
    APPROVED("已通過"),
    SHIPPED("已出貨"),
    DELIVERED("已送達"),
    PENDING_PAYMENT("等待付款");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
