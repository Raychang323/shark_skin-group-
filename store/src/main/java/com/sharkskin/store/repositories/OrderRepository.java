package com.sharkskin.store.repositories;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderStatus; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Spring Data JPA will automatically implement this method based on its name.
    List<Order> findByEmail(String email);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);
}