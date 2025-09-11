package com.sharkskin.store.service;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;
import com.sharkskin.store.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final List<Order> orderList = new ArrayList<>();

    public OrderService() {
        // Create fake products
        Product product1 = new Product("p001", "鯊魚皮外套", 3000, "url1");
        Product product2 = new Product("p002", "鯊魚造型帽", 800, "url2");
        Product product3 = new Product("p003", "鯊魚腳蹼", 1200, "url3");

        // Create fake order 1
        List<OrderItem> orderItems1 = Arrays.asList(
                new OrderItem(product1, 1),
                new OrderItem(product2, 2)
        );
        double total1 = orderItems1.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        Order order1 = new Order("ORD001", "customer1@example.com", "處理中", orderItems1, total1);

        // Create fake order 2
        List<OrderItem> orderItems2 = Arrays.asList(
                new OrderItem(product3, 1)
        );
        double total2 = orderItems2.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        Order order2 = new Order("ORD002", "customer2@example.com", "已出貨", orderItems2, total2);

        orderList.add(order1);
        orderList.add(order2);
    }

    public Optional<Order> findOrderByOrderNumberAndEmail(String orderNumber, String email) {
        return orderList.stream()
                .filter(order -> order.getOrderNumber().equalsIgnoreCase(orderNumber) && order.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
