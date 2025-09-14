package com.sharkskin.store.model;

public enum PaymentMethod {
    CASH_ON_DELIVERY("貨到付款"),
    LINE_PAY("LINE Pay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
