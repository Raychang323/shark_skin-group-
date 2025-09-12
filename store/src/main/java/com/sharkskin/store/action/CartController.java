package com.sharkskin.store.action;

import com.sharkskin.store.dto.CartSummaryDto; // Add this import
import com.sharkskin.store.service.CartService;
import com.sharkskin.store.model.Cart;
import com.sharkskin.store.service.GcsImageUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CartController {

    private final CartService cartService;
    private final GcsImageUploadService gcsImageUploadService;

    @Autowired
    public CartController(CartService cartService, GcsImageUploadService gcsImageUploadService) {
        this.cartService = cartService;
        this.gcsImageUploadService = gcsImageUploadService;
    }

    // Display the full cart page
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login"; // Not logged in, redirect to login page
        }
        Cart cart = cartService.getCart(session);

        if (cart != null && cart.getItems() != null) {
            // Generate signed URLs for each product image in the cart
            for (com.sharkskin.store.model.CartItem item : cart.getItems()) {
                com.sharkskin.store.model.Product product = item.getProduct();
                if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
                    // Assuming we only need the first image for the cart view
                    com.sharkskin.store.model.ProductImage image = product.getImages().get(0);
                    String fileName = image.getImageUrl();
                    String signedUrl = gcsImageUploadService.generateSignedUrl(fileName, 60); // 60-minute expiration
                    image.setSignedUrl(signedUrl);
                }
            }

            Map<String, Integer> productStock = cart.getItems().stream()
                .filter(item -> item != null && item.getProduct() != null && item.getProduct().getP_id() != null)
                .collect(Collectors.toMap(
                    item -> item.getProduct().getP_id(),
                    item -> item.getProduct().getStock(),
                    (existing, replacement) -> existing
                ));
            model.addAttribute("productStock", productStock);
        }

        model.addAttribute("cart", cart);
        return "cart"; // This will map to cart.html
    }

    // Add product to cart
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addProductToCart(@RequestParam String productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Not logged in, return 401 Unauthorized
        }
        cartService.addProductToCart(session, productId, quantity);
        return new ResponseEntity<>(HttpStatus.OK); // Product added successfully
    }

    // Update product quantity in cart
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItemQuantity(@RequestParam String productId, @RequestParam int quantityChange, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Not logged in, return 401 Unauthorized
        }
        try {
            cartService.updateProductQuantityByChange(session, productId, quantityChange);
            return new ResponseEntity<>(HttpStatus.OK); // Product quantity updated successfully
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Remove product from cart
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeProductFromCart(@RequestParam String productId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Not logged in, return 401 Unauthorized
        }
        try {
            cartService.removeProductFromCart(session, productId);
            return new ResponseEntity<>(HttpStatus.OK); // Product removed successfully
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // REST endpoint for mini-cart summary
    @GetMapping("/api/cart/summary")
    @ResponseBody // This annotation makes Spring return data directly, not a view name
    public CartSummaryDto getCartSummary(HttpSession session) { // Changed return type
        return cartService.getCartSummaryDto(session); // Call the new DTO method
    }
}
