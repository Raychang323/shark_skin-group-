package com.sharkskin.store.repositories;

import com.sharkskin.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Spring Data JPA will automatically implement this method based on its name.
    List<Order> findByEmail(String email);

}
