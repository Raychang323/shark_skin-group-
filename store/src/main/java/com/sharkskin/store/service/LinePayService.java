package com.sharkskin.store.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkskin.store.config.LinePayConfig;
import com.sharkskin.store.linepay.LinepayResponse;
import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderItem;

public class LinePayService {
	
    private JavaMailSender mailsend;
	
    // ==========================
    // ⚠️ 這裡請自己設定
	private static final String CHANNEL_ID = "2007814032";
	private String channelId = "2007814032";
	private static final String CHANNEL_SECRET = "ba4bc4b681b1d881df76b8ece6b93085";
	private String channelSecret = "ba4bc4b681b1d881df76b8ece6b93085";
    private static final String API_BASE_URL = "https://sandbox-api-pay.line.me"; // sandbox 或正式
    private static final String REQUEST_API = "/v3/payments/request";

    private static final String CONFIRM_URL = "http://localhost:8080/linepay/confirm";
    private static final String CANCEL_URL = "http://localhost:8080/linepay/cancel";
    private static final String CURRENCY = "TWD";
    // ==========================

    private final RestTemplate restTemplate;


    /**
     * 建立付款請求
     * @param order 整筆訂單
     * @return 付款 URL，如果失敗返回 null
     */
    public LinePayService() {
        this.restTemplate = new RestTemplate();
    }

    public String requestPayment(Order order) throws Exception {
    	System.out.println("Rp:"+order!=null);//T
    	
    	
      
        	// 2️⃣ 建立 request body
            Map<String, Object> body = new HashMap<>();
            body.put("amount", (int) order.getTotalPrice()); // 整數
            System.out.println("totalprice"+ order.getTotalPrice()); //T
            body.put("currency", CURRENCY);
            System.out.println( CURRENCY); //T
//            body.put("orderId", order.getOrderNumber());
            body.put("orderId", order.getOrderNumber() + "-" + System.currentTimeMillis());
            System.out.println( "rp:"+order.getOrderNumber()); //T

            // 商品 package
            Map<String, Object> productPackage = new HashMap<>();
            productPackage.put("id", "package-1");

            int packageAmount = 0;
            List<Map<String, Object>> products = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> product = new HashMap<>();
                product.put("name", item.getProduct().getName());
                product.put("quantity", item.getQuantity());
                product.put("price", item.getProduct().getPrice());
                products.add(product);

                packageAmount += item.getProduct().getPrice() * item.getQuantity();
            }

            productPackage.put("products", products);
            productPackage.put("amount", packageAmount); 
            body.put("packages", List.of(productPackage));

            // redirect URLs
            body.put("redirectUrls", Map.of(
                    "confirmUrl", CONFIRM_URL,
                    "cancelUrl", CANCEL_URL
            ));
            System.out.println("body" + body);

            // 3️⃣ 轉成 JSON 字串（緊湊格式）
            ObjectMapper mapper = new ObjectMapper();
            String bodyJson = mapper.writeValueAsString(body);
            System.out.println("badyJ" + bodyJson);
        	
         // 4️⃣ 計算 HMAC-SHA256 簽章
            String nonce = UUID.randomUUID().toString();
//          String signature = generateSignature(LinePayConfig.REQUEST_API, bodyJson, nonce);
            String signature = generateSignature(LinePayConfig.REQUEST_API, bodyJson, nonce);
            System.out.println("nonce:"+nonce);
            System.out.println("sig:"+signature);
        	
        	
            // 1️⃣ 建立請求 URL
//           String url = API_BASE_URL + REQUEST_API;
            URL url = new URL(LinePayConfig.API_BASE_URL + LinePayConfig.REQUEST_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.out.println("tURL"+ url); //T


            
            
            
            // 5️⃣ 設定 headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-LINE-ChannelId", LinePayConfig.CHANNEL_ID);
            conn.setRequestProperty("X-LINE-Authorization-Nonce", nonce);
            conn.setRequestProperty("X-LINE-Authorization", signature);
            conn.setDoOutput(true);
//            HttpEntity<String> requestEntity = new HttpEntity<>(bodyJson, headers);

            // 6️⃣ 發送請求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = bodyJson.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
         // 讀取回應
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // 解析回應取得付款網址
            return parsePaymentUrl(response.toString());
    }
        
         /* 產生 HMAC 簽章
         */
        private String generateSignature(String uri, String requestBody, String nonce) throws Exception {
            String message = LinePayConfig.CHANNEL_SECRET + uri + requestBody + nonce;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                LinePayConfig.CHANNEL_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }

        /**
         * 解析 Line Pay 回應，取得付款網址
         */
        private String parsePaymentUrl(String responseJson) throws Exception {
            // 簡單的 JSON 解析（實際專案建議使用 JSON 函式庫）
            if (responseJson.contains("\"returnCode\":\"0000\"")) {
                // 成功回應，提取付款網址
                int startIndex = responseJson.indexOf("\"web\":\"") + 7;
                int endIndex = responseJson.indexOf("\"", startIndex);

                if (startIndex > 6 && endIndex > startIndex) {
                    return responseJson.substring(startIndex, endIndex).replace("\\\\", "");
                }
            }

            // 付款請求失敗
            throw new Exception("Line Pay 付款請求失敗：" + responseJson);
        }

        /**
         * 跳脫 JSON 特殊字元
         */
        private String escapeJson(String text) {
            if (text == null) return "";
            return text.replace("\"", "\\\"")
                      .replace("\\", "\\\\")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t");

        }
        public LinepayResponse confirmPayment(String transactionId, double amount) throws Exception {
        	// 1️⃣ 建立請求 URL
        	String confirmApi = String.format("/v3/payments/%s/confirm", transactionId);
        	String url = LinePayConfig.API_BASE_URL + confirmApi;

            // 2️⃣ 建立 request body
            Map<String, Object> body = new HashMap<>();
            body.put("amount", (int) amount);
            body.put("currency", LinePayConfig.CURRENCY);

            // 3️⃣ 轉成 JSON 字串（緊湊格式）
            ObjectMapper mapper = new ObjectMapper();
            String bodyJson = mapper.writeValueAsString(body);

            // 4️⃣ 計算 HMAC-SHA256 簽章
            String nonce = UUID.randomUUID().toString();
            String signature = generateSignature(confirmApi, bodyJson, nonce);

            // 5️⃣ 設定 headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-LINE-ChannelId", LinePayConfig.CHANNEL_ID);
            headers.set("X-LINE-Authorization-Nonce", nonce);
            headers.set("X-LINE-Authorization", signature);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 6️⃣ 發送請求
            ResponseEntity<LinepayResponse> responseEntity = 
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, LinepayResponse.class);

            return responseEntity.getBody();
        }
        public void payuEmail(Order order) {
            SimpleMailMessage message = new SimpleMailMessage();        
                message.setTo(order.getEmail()); //設置收件人信箱
                message.setSubject("鯊皮訂單確定"); //設置信箱主題
                message.setText("您好，感謝您使用SharkShop，下面是你的訂單連結:/n http://localhost:8080/lookup-order?orderNumber="+order.getOrderNumber()+"&email="+order.getEmail()); //設置信箱內容
                mailsend.send(message); //發送郵件
             }
}