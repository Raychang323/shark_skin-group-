package com.sharkskin.store.action;

import com.sharkskin.store.linepay.LinepayResponse;
import com.sharkskin.store.model.Cart; // Add this import
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderStatus;
import com.sharkskin.store.model.PaymentMethod; // Add this import
import com.sharkskin.store.model.Product;
import com.sharkskin.store.service.CartService; // Add this import
import com.sharkskin.store.service.LinePayService;
import com.sharkskin.store.service.OrderService;

import jakarta.servlet.http.HttpSession;
import javax.annotation.PostConstruct;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/linepay")
public class LinePayController {
   
    @Autowired
    private LinePayService linePayService;
    @Autowired
    private OrderService orderService;
    @Autowired // Add this
    private CartService cartService; // Add this

    

    
    
    @GetMapping("/pay")
    public String pay(@RequestParam String orderNumber,HttpSession session,Model model) throws Exception {
    	System.out.println("IIDD:"+orderNumber);
    	
        Optional<Order> orderOptional = orderService.findByOrderNumber(orderNumber);
    	System.out.println(orderOptional.isPresent());
        if (orderOptional.isEmpty()) {
            model.addAttribute("message", "訂單不存在");
            return "my_orders";
        }
        Order order = orderOptional.get();
        String paymentUrl = linePayService.requestPayment(order);;
        
        
    	System.out.println("url"+paymentUrl);
        if (paymentUrl != null) {
            model.addAttribute("paymentUrl", paymentUrl);
            return "linepay_processing";
        } else {
            model.addAttribute("message", "LinePay付款發生錯誤");
            return "my_orders";
        }
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("transactionId") String transactionId,
                          @RequestParam("orderId") String orderNumber,
                          Model model) {

        String actualOrderNumber = orderNumber.substring(0, orderNumber.lastIndexOf("-"));
        System.out.println("這裡:"+actualOrderNumber);

        Optional<Order> orderOptional = orderService.findByOrderNumber(actualOrderNumber );
        System.out.println("這裡:"+(orderOptional.isPresent() ? orderOptional.get().getOrderNumber() : "Not Found"));

        if (orderOptional.isEmpty()) {
            model.addAttribute("error", "訂單不存在");
            model.addAttribute("transactionId", transactionId);
            System.out.println("這裡");
            return "linepay_fail";
        }
        Order order = orderOptional.get();
        LinepayResponse response=null;
		try {
			response = linePayService.confirmPayment(transactionId, order.getTotalPrice());
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (response != null && response.getReturnCode().equals("0000")) {
            // 付款成功
            order.setStatus(OrderStatus.APPROVED);
            System.out.println(order.getStatus());

            order.getItems().forEach(item -> {
                Product p = item.getProduct();
                p.setStock(p.getStock() - item.getQuantity());
            });
            orderService.saveOrder(order);
//            linePayService.payuEmail(order);
            model.addAttribute("orderNumber", order.getOrderNumber());
            return "linepay_success";
        } else {
            System.out.println("這裡2");

            model.addAttribute("transactionId", transactionId);
            return "linepay_fail";
        }
    }
   
}