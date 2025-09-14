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

    /**
     * Finds all orders. Used for admin order management.
     * @return A list of all orders.
     */
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findOrdersByStatus(String status) {
        try {
            // Try to find by enum name first
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            // If not found by enum name, try to find by display name
            for (OrderStatus os : OrderStatus.values()) {
                if (os.getDisplayName().equalsIgnoreCase(status)) {
                    return orderRepository.findByStatus(os);
                }
            }
            // If still not found, handle as invalid status
            return List.of(); // Or throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public void updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        Optional<Order> orderOptional = orderRepository.findByOrderNumber(orderNumber);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(newStatus);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order not found with order number: " + orderNumber);
        }
    }

    public void bulkUpdateOrderStatus(List<String> orderNumbers, OrderStatus newStatus) {
        for (String orderNumber : orderNumbers) {
            Optional<Order> orderOptional = orderRepository.findByOrderNumber(orderNumber);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                order.setStatus(newStatus);
                orderRepository.save(order);
            } else {
                // Optionally log a warning if an order is not found
                System.out.println("Warning: Order not found with order number: " + orderNumber + " during bulk update.");
            }
        }
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
