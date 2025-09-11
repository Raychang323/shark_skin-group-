package com.sharkskin.store.config;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        // === Create Users ===
        UserModel user1 = createUserIfNotFound("test1", "1234", "testuser1@example.com", "USER");
        createUserIfNotFound("test2", "1234", "testuser2@example.com", "USER");
        createUserIfNotFound("admin", "adminpass", "admin@example.com", "ADMIN"); // Admin user

        // === Create Products ===
        Product p1 = createProductIfNotFound("p001", "鯊魚皮外套", 3000, 100, Arrays.asList(
                "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+1_View1",
                "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+1_View2"
        ));
        Product p2 = createProductIfNotFound("p002", "鯊魚造型帽", 800, 50, Arrays.asList(
                "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+2_View1"
        ));
        Product p3 = createProductIfNotFound("p003", "鯊魚腳蹼", 1200, 200, Arrays.asList(
                "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+3_View1"
        ));

        // === Create an Order for testuser1 if they have no orders ===
        if (user1 != null && orderRepository.findByEmail(user1.getEmail()).isEmpty()) {
            Order order1 = new Order("ORD001", user1.getEmail(), "處理中", 0);

            OrderItem item1 = new OrderItem(p1, 1);
            OrderItem item2 = new OrderItem(p2, 2);

            order1.addOrderItem(item1);
            order1.addOrderItem(item2);

            double totalPrice = order1.getItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
            order1.setTotalPrice(totalPrice);

            orderRepository.save(order1);
            System.out.println("Created test order ORD001 for user: " + user1.getUsername());
        }
    }

    private UserModel createUserIfNotFound(String username, String password, String email, String role) {
        if (!userRepository.existsByUsername(username)) {
            UserModel user = new UserModel();
            user.setUsername(username);
            user.setPassword(password); // Plain text password
            user.setEmail(email);
            user.setRole(role); // Set the role
            System.out.println("Created test user: " + username + " with role: " + role);
            return userRepository.save(user);
        }
        return userRepository.findByUsername(username);
    }

    private Product createProductIfNotFound(String id, String name, int price, int stock, java.util.List<String> imageUrls) {
        return productRepository.findById(id).orElseGet(() -> {
            Product product = new Product(id, name, price, stock);
            imageUrls.forEach(url -> product.addImage(new ProductImage(url, product)));
            System.out.println("Created test product: " + name);
            return productRepository.save(product);
        });
    }
}
