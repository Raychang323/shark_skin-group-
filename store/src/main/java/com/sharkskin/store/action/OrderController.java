package com.sharkskin.store.action;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order-lookup")
    public String showOrderLookupPage() {
        return "order_lookup";
    }

    @PostMapping("/order-lookup")
    public String findOrder(@RequestParam String orderNumber, @RequestParam String email, Model model) {
        Optional<Order> orderOptional = orderService.findOrderByOrderNumberAndEmail(orderNumber, email);

        if (orderOptional.isPresent()) {
            model.addAttribute("order", orderOptional.get());
        } else {
            model.addAttribute("error", "訂單號碼或email錯誤");
        }
        return "order_lookup";
    }
}
