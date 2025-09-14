package com.sharkskin.store.action;

import com.sharkskin.store.linepay.LinepayResponse;
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.service.LinePayService;
import com.sharkskin.store.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/linepay")
public class LinePayController  extends HttpServlet{
   
    @Autowired
    private LinePayService linePayService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/pay")
    public String pay(@RequestParam String orderNumber,HttpSession session,Model model) throws Exception {
    	System.out.println(orderNumber);
        Order order = orderService.findByOrderNumber(orderNumber);
    	System.out.println(order!=null);
        if (order == null) {
            model.addAttribute("message", "訂單不存在");
            return "my_orders";
        }
        LinePayService linePayService = new LinePayService();
        String paymentUrl = linePayService.requestPayment(order);;
        
        
    	System.out.println("url"+paymentUrl);
        if (paymentUrl != null) {
            return "redirect:" + paymentUrl;
        } else {
            model.addAttribute("message", "LinePay付款發生錯誤");
            return "my_orders";
        }
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("transactionId") String transactionId,
                          @RequestParam("orderId") String orderNumber,
                          Model model) {
		System.out.println("TTID"+transactionId);

        String orderId = orderNumber.split("-")[0];
        Order order = orderService.findByOrderNumber(orderId );
        if (order == null) {
            model.addAttribute("error", "訂單不存在");
            return "linepay_fail";
        }
        LinepayResponse response=null;
		try {
			response = linePayService.confirmPayment(transactionId, order.getTotalPrice());
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (response != null) {
            // 付款成功
            order.setStatus("已付款");
            order.getItems().forEach(item -> {
                Product p = item.getProduct();
                p.setStock(p.getStock() - item.getQuantity());
            });
            orderService.saveOrder(order);
            linePayService.payuEmail(order);
            model.addAttribute("orderNumber", order.getOrderNumber());
            return "linepay_success";
        } else {
            model.addAttribute("orderNumber", order.getOrderNumber());
            return "linepay_fail";
        }
    }
   
}