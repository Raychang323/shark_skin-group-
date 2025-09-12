package com.sharkskin.store.action;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.service.LinePayService;
import com.sharkskin.store.service.OrderService;
import com.sharkskin.store.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/linepay")
public class LinePayController {

    @Autowired
    private LinePayService linePayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // 建立付款請求
    @PostMapping("/request")
    public Object requestPayment(@RequestParam String orderNumber, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "未登入";
        }

        Order order = orderService.findByOrderNumber(orderNumber);
        if (order == null) return "訂單不存在";

        return linePayService.requestPayment(order);
    }

    // LINE PAY Webhook 確認付款
    @PostMapping("/confirm")
    public String confirmPayment(@RequestParam String transactionId, @RequestParam String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber);
        if (order == null) return "訂單不存在";

        var result = linePayService.confirmPayment(transactionId, order);
        // 成功後更新訂單狀態
        order.setStatus("已付款");
        orderService.saveOrder(order);

        return "付款完成";
    }
}
