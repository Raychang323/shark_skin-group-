package com.sharkskin.store.config;

public class LinePayConfig {
   public static final String CHANNEL_ID = "2007814032";
   public static final String CHANNEL_SECRET = "ba4bc4b681b1d881df76b8ece6b93085";
   public static final String API_BASE_URL = "https://sandbox-api-pay.line.me";

   public static final String REQUEST_API = "/v3/payments/request";
   public static final String CONFIRM_API = "/v3/payments/{transactionId}/confirm";

   public static final String CONFIRM_URL = "http://192.168.1.105:8080/linepay/confirm";
   public static final String CANCEL_URL = "http://192.168.1.105:8080/linepay/cancel";

   public static final String CURRENCY = "TWD";
   public static final String MERCHANT_NAME = "購物商城測試";

   private LinePayConfig() {}
}