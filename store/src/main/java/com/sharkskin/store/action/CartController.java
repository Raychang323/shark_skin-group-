package com.sharkskin.store.action;

import com.sharkskin.store.model.Cart;
import com.sharkskin.store.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Display the full cart page
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Cart cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart"; // This will map to cart.html
    }

    // Add product to cart
    @PostMapping("/cart/add")
    public String addProductToCart(@RequestParam String productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login"; // Not logged in, redirect to login page
        }
        cartService.addProductToCart(session, productId, quantity);
        return "redirect:/products"; // Redirect back to product list or a confirmation page
    }

    // Update product quantity in cart
    @PostMapping("/cart/update")
    public String updateCartItemQuantity(@RequestParam String productId, @RequestParam int quantity, HttpSession session) {
        cartService.updateProductQuantity(session, productId, quantity);
        return "redirect:/cart"; // Redirect back to cart page
    }

    // Remove product from cart
    @PostMapping("/cart/remove")
    public String removeProductFromCart(@RequestParam String productId, HttpSession session) {
        cartService.removeProductFromCart(session, productId);
        return "redirect:/cart"; // Redirect back to cart page
    }

    // REST endpoint for mini-cart summary
    @GetMapping("/api/cart/summary")
    @ResponseBody // This annotation makes Spring return data directly, not a view name
    public Cart getCartSummary(HttpSession session) {
        return cartService.getCart(session);
    }
}
