package com.sharkskin.store.service;

import com.sharkskin.store.model.Cart;
import com.sharkskin.store.model.CartItem;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.repositories.CartItemRepository;
import com.sharkskin.store.repositories.CartRepository;
import com.sharkskin.store.repositories.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getCart(HttpSession session) {
        String userId = (String) session.getAttribute("username"); // Assuming username is userId
        String sessionId = session.getId();

        Optional<Cart> cartOptional;

        if (userId != null) {
            cartOptional = cartRepository.findByUserId(userId);
            if (cartOptional.isEmpty()) {
                // If user is logged in but has no cart, check if there's a guest cart for this session
                Optional<Cart> guestCartOptional = cartRepository.findBySessionId(sessionId);
                if (guestCartOptional.isPresent()) {
                    Cart guestCart = guestCartOptional.get();
                    guestCart.setUserId(userId); // Assign guest cart to logged-in user
                    guestCart.setSessionId(null); // Clear session ID
                    return cartRepository.save(guestCart);
                }
            }
        } else {
            cartOptional = cartRepository.findBySessionId(sessionId);
        }

        return cartOptional.orElseGet(() -> {
            Cart newCart = new Cart();
            if (userId != null) {
                newCart.setUserId(userId);
            } else {
                newCart.setSessionId(sessionId);
            }
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    public Cart addProductToCart(HttpSession session, String productId, int quantity) {
        Cart cart = getCart(session);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingCartItem.isPresent()) {
            CartItem item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            cart.addCartItem(newItem); // Add to cart's item list
        }
        return cartRepository.save(cart); // Save cart to update timestamps and relationships
    }

    @Transactional
    public Cart updateProductQuantity(HttpSession session, String productId, int quantity) {
        Cart cart = getCart(session);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not in cart"));

        if (quantity <= 0) {
            cart.removeCartItem(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeProductFromCart(HttpSession session, String productId) {
        Cart cart = getCart(session);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not in cart"));

        cart.removeCartItem(item);
        cartItemRepository.delete(item);
        return cartRepository.save(cart);
    }
}
