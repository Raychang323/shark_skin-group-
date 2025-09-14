package com.sharkskin.store.dto;

import com.sharkskin.store.model.OrderStatus;
import java.util.List;

public class BulkOrderStatusUpdateDto {
    private List<String> orderNumbers;
    private OrderStatus newStatus;

    // Getters and Setters
    public List<String> getOrderNumbers() {
        return orderNumbers;
    }

    public void setOrderNumbers(List<String> orderNumbers) {
        this.orderNumbers = orderNumbers;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
}
