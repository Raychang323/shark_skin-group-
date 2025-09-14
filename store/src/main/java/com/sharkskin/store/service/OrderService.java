package com.sharkskin.store.service;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Finds a single order by order number and email. Used for the public order lookup.
     * Note: This is not very efficient as it loads all orders for an email first.
     * A custom query would be better for performance in a real application.
     */
    public Optional<Order> findOrderByOrderNumberAndEmail(String orderNumber, String email) {
        List<Order> userOrders = orderRepository.findByEmail(email);
        return userOrders.stream()
                .filter(order -> order.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst();
    }

    /**
     * Finds all orders for a given user email. Used for the logged-in user's "My Orders" page.
     * @param email The email of the user.
     * @return A list of orders.
     */
    public List<Order> findByUserEmail(String email) {
        return orderRepository.findByEmail(email);
    }
        //for linepay
        // 依 orderNumber 查找單筆訂單
        public Order findByOrderNumber(String orderNumber) {
            return orderRepository.findByOrderNumber(orderNumber);
        }
        //更新訂單
        public void saveOrder(Order order) {
            orderRepository.save(order);
    }
}