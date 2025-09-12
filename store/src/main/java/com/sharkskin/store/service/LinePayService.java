package com.sharkskin.store.service;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sharkskin.store.model.Order;

@Service
public class LinePayService {

    @Value("${linepay.channelId}")
    private String channelId;

    @Value("${linepay.channelSecret}")
    private String channelSecret;

    @Value("${linepay.sandboxBaseUrl}")
    private String sandboxBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    // 建立付款請求
    public Map<String, Object> requestPayment(Order order) {
        String url = sandboxBaseUrl + "/v3/payments/request";

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", (int) order.getTotalPrice()); // LINE Pay 需要整數
        payload.put("currency", "TWD");
        payload.put("orderId", order.getOrderNumber());
        payload.put("packages", new Object[] {
            Map.of(
                "id", "package-1",
                "amount", (int) order.getTotalPrice(),
                "products", order.getItems().stream().map(item ->
                    Map.of(
                        "name", item.getProduct().getName(),
                        "quantity", item.getQuantity(),
                        "price", item.getProduct().getPrice()
                    )
                ).toArray()
            )
        });
        payload.put("redirectUrls", Map.of(
            "confirmUrl", "https://localhost:8080/confirm",
            "cancelUrl", "https://localhost:8080/home"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-ChannelId", channelId);
        headers.set("X-LINE-ChannelSecret", channelSecret);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        return response.getBody();
    }

    // 確認付款
    public Map<String, Object> confirmPayment(String transactionId, Order order) {
        String url = sandboxBaseUrl + "/v3/payments/" + transactionId + "/confirm";

        Map<String, Object> payload = Map.of(
            "amount", (int) order.getTotalPrice(),
            "currency", "TWD"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-ChannelId", channelId);
        headers.set("X-LINE-ChannelSecret", channelSecret);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        return response.getBody();
    }
}