package com.sharkskin.store.service;

import com.sharkskin.store.model.Cart;
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;
import com.sharkskin.store.model.OrderStatus;
import com.sharkskin.store.model.PaymentMethod;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public Order createOrder(Cart cart, UserModel user, PaymentMethod paymentMethod) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setEmail(user.getEmail());
        order.setTotalPrice(cart.getTotalPrice());
        order.setPaymentMethod(paymentMethod);

        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            order.setStatus(OrderStatus.PROCESSING);
        } else {
            order.setStatus(OrderStatus.PENDING_PAYMENT); // Or some other initial status for online payments
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);
        orderRepository.save(order);
        return order;
    }

    private String generateOrderNumber() {
        // Simple order number generation, you might want something more robust
        return "ORD-" + System.currentTimeMillis();
    }
}
