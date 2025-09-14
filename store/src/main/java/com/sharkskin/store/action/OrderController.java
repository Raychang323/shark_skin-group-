package com.sharkskin.store.action;

import com.sharkskin.store.model.Cart;
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.PaymentMethod;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.service.CartService;
import com.sharkskin.store.service.OrderService;
import com.sharkskin.store.service.UserService;
import com.sharkskin.store.service.GcsImageUploadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private GcsImageUploadService gcsImageUploadService;

    // Public order lookup
    @GetMapping("/order-lookup")
    public String showOrderLookupPage() {
        return "order_lookup";
    }

    @PostMapping("/order-lookup")
    public String findOrder(@RequestParam String orderNumber, @RequestParam String email, Model model) {
        Optional<Order> orderOptional = orderService.findOrderByOrderNumberAndEmail(orderNumber, email);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            // Generate signed URLs for each product image in the order items
            order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().forEach(image -> {
                        String signedUrl = gcsImageUploadService.generateSignedUrl(image.getImageUrl(), 60); // 60-minute expiration
                        image.setSignedUrl(signedUrl);
                    });
                }
            });
            model.addAttribute("order", order);
        } else {
            model.addAttribute("error", "訂單號碼或email錯誤");
        }
        return "order_lookup";
    }

    // Logged-in user's order history
    @GetMapping("/my-orders")
    public String showMyOrders(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login"; // Not logged in, redirect to login
        }

        UserModel user = userService.getUserByUsername(username);
        if (user != null) {
            List<Order> orders = orderService.findByUserEmail(user.getEmail());
            orders.forEach(order -> {
                order.getItems().forEach(item -> {
                    if (item.getProduct() != null && item.getProduct().getImages() != null) {
                        item.getProduct().getImages().forEach(image -> {
                            String signedUrl = gcsImageUploadService.generateSignedUrl(image.getImageUrl(), 60); // 60-minute expiration
                            image.setSignedUrl(signedUrl);
                        });
                    }
                });
            });
            model.addAttribute("orders", orders);
        }

        return "my_orders";
    }

    @PostMapping("/create-order")
    public String createOrder(HttpSession session, @RequestParam String paymentMethod) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        UserModel user = userService.getUserByUsername(username);
        Cart cart = cartService.getCart(session);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        PaymentMethod pm = PaymentMethod.valueOf(paymentMethod);
        Order order = orderService.createOrder(cart, user, pm);

        if (pm == PaymentMethod.CASH_ON_DELIVERY) {
            cartService.clearCart(session);
            return "redirect:/payment-success";
        } else {
            // Redirect to Line Pay
            return "redirect:/linepay/pay?orderNumber=" + order.getOrderNumber();
        }
    }

    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment_success";
    }
}
