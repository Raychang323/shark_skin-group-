package com.sharkskin.store.service;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;
import com.sharkskin.store.model.PaymentMethod;
import com.sharkskin.store.model.OrderStatus;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import com.sharkskin.store.model.Cart;
import com.sharkskin.store.model.CartItem;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.repositories.CartItemRepository;
import com.sharkskin.store.repositories.CartRepository;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.repositories.UserRepository;
import com.sharkskin.store.service.GcsImageUploadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import com.sharkskin.store.dto.CartSummaryDto;
import com.sharkskin.store.dto.CartItemDto;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private final UserRepository userRepository;
    private final GcsImageUploadService gcsImageUploadService; // Add this
    private final OrderRepository orderRepository; // Add this field

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, GcsImageUploadService gcsImageUploadService, OrderRepository orderRepository) { // Add OrderRepository
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.gcsImageUploadService = gcsImageUploadService; // Add this
        this.orderRepository = orderRepository; // Assign it
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
    public Cart updateProductQuantityByChange(HttpSession session, String productId, int quantityChange) {
        Cart cart = getCart(session);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not in cart"));

        int newQuantity = item.getQuantity() + quantityChange;

        if (newQuantity <= 0) {
            cart.removeCartItem(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
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

    @Transactional
    public void mergeCarts(HttpSession session) {
        String userId = (String) session.getAttribute("username");
        String sessionId = session.getId();

        if (userId == null) {
            return; // Only merge if a user is logged in
        }

        Optional<Cart> userCartOptional = cartRepository.findByUserId(userId);
        Optional<Cart> guestCartOptional = cartRepository.findBySessionId(sessionId);

        if (guestCartOptional.isPresent()) {
            Cart guestCart = guestCartOptional.get();
            if (userCartOptional.isPresent()) {
                // User has a cart, merge guest cart items into user's cart
                Cart userCart = userCartOptional.get();
                guestCart.getItems().forEach(guestItem -> {
                    Optional<CartItem> existingUserItem = userCart.getItems().stream()
                            .filter(userItem -> userItem.getProduct().getP_id().equals(guestItem.getProduct().getP_id()))
                            .findFirst();
                    if (existingUserItem.isPresent()) {
                        existingUserItem.get().setQuantity(existingUserItem.get().getQuantity() + guestItem.getQuantity());
                        cartItemRepository.save(existingUserItem.get());
                    } else {
                        guestItem.setCart(userCart); // Assign to user's cart
                        cartItemRepository.save(guestItem);
                        userCart.addCartItem(guestItem);
                    }
                });
                cartRepository.delete(guestCart); // Delete the guest cart
                cartRepository.save(userCart); // Save the updated user cart
            } else {
                // User does not have a cart, assign guest cart to user
                guestCart.setUserId(userId);
                guestCart.setSessionId(null);
                cartRepository.save(guestCart);
            }
        }
    }

    public com.sharkskin.store.dto.CartSummaryDto getCartSummaryDto(HttpSession session) {
        Cart cart = getCart(session); // Get the full cart entity
        List<com.sharkskin.store.dto.CartItemDto> itemDtos = new java.util.ArrayList<>();
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                String imageUrl = null;
                // Check if product and images are not null and not empty
                if (item.getProduct() != null && item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                    // Generate a signed URL for the first image
                    String fileName = item.getProduct().getImages().get(0).getImageUrl();
                    imageUrl = gcsImageUploadService.generateSignedUrl(fileName, 60); // 60-minute expiration
                }
                itemDtos.add(new com.sharkskin.store.dto.CartItemDto(
                    item.getProduct().getP_id(),
                    item.getProduct().getName(),
                    imageUrl, // Use the generated signed URL
                    item.getProduct().getPrice(),
                    item.getQuantity()
                ));
            }
        }
        return new com.sharkskin.store.dto.CartSummaryDto(itemDtos, cart.getTotalPrice());
    }

    @Transactional
    public void clearCart(HttpSession session) {
        Cart cart = getCart(session);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public Order createOrderFromCart(HttpSession session, PaymentMethod paymentMethod, OrderStatus orderStatus) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new IllegalStateException("User not logged in.");
        }

        Cart cart = getCart(session);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found."));

        // Create a new Order
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8)); // Generate a unique order number
        order.setEmail(user.getEmail());
        order.setStatus(orderStatus);
        order.setPaymentMethod(paymentMethod);
        order.setTotalPrice(cart.getTotalPrice());
        order.setUser(user); // Link the order to the user

        // Convert CartItems to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order); // Link the order item to the order
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        orderRepository.save(order); // Save the order and its items (due to cascade)

        // Clear the cart after order is created
        clearCart(session);
        return order;
    }
}

