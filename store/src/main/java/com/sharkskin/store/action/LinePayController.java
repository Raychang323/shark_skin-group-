package com.sharkskin.store.action;

import com.sharkskin.store.config.CartForm;
import com.sharkskin.store.config.CartItemForm;
import com.sharkskin.store.linepay.LinepayResponse;
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.service.LinePayService;
import com.sharkskin.store.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
    private ProductRepository productRepository; // 正確的名稱
    @Autowired
    private OrderService orderService;
    
    
    @PostMapping("/create-order")
    public String createOrder(@ModelAttribute CartForm cartForm, Model model, HttpSession session) {
        if (cartForm.getItems() == null || cartForm.getItems().isEmpty()) {
            model.addAttribute("error", "購物車為空，無法建立訂單");
            return "cart";
        }

        // 先建立訂單
        Order order = new Order();
        order.setStatus("未付款");
        String code = String.format("%06d", new Random().nextInt(999999));

        order.setOrderNumber(code); // 生成唯一訂單號
        order.setEmail("Unknow");
        double totalPrice = 0;

        for (CartItemForm ci : cartForm.getItems()) {
            String productId = ci.getProductId();
            Integer quantity = ci.getQuantity();
            if (quantity == null || quantity < 1) continue;

            Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));

            OrderItem orderItem = new OrderItem(product, quantity);
            order.addOrderItem(orderItem);

            totalPrice += product.getPrice() * quantity;
        }

        order.setTotalPrice(totalPrice);
        orderService.saveOrder(order);

        System.out.println("訂單號碼: " + order.getOrderNumber());
        return "redirect:/linepay/pay?orderNumber=" + order.getOrderNumber();
    }

    
    
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

        String orderId = orderNumber.split("-")[0];
        System.out.println("這裡:"+orderId);

        Optional<Order> orderOptional = orderService.findByOrderNumber(orderId );
        System.out.println("這裡:"+(orderOptional.isPresent() ? orderOptional.get().getOrderNumber() : "Not Found"));

        if (orderOptional.isEmpty()) {
            model.addAttribute("error", "訂單不存在");
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
        if (response != null) {
            // 付款成功
            order.setStatus("已付款");
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

            model.addAttribute("orderNumber", order.getOrderNumber());
            return "linepay_fail";
        }
    }
   
}