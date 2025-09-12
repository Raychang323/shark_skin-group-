package com.sharkskin.store.repositories;

import com.sharkskin.store.model.CartItem;
import com.sharkskin.store.model.Cart;
import com.sharkskin.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
