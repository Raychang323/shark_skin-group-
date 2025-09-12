//package com.sharkskin.store.config;
//
//public class LinePayConfig {
//    // Line Pay 測試環境設定
//    public static final String CHANNEL_ID = "2007814032";           // 測試用 Channel ID
//    public static final String CHANNEL_SECRET = "ba4bc4b681b1d881df76b8ece6b93085";  // 測試用 Channel Secret
//    public static final String API_BASE_URL = "https://sandbox-api-pay.line.me";
//
//    // API 端點
//    public static final String REQUEST_API = "/v3/payments/request";
//    public static final String CONFIRM_API = "/v3/payments/{transactionId}/confirm";
//
//    // 回調網址（需要改為您的實際網址）
//    public static final String CONFIRM_URL = "http://localhost:8080/demo/checkout?action=confirm";
//    public static final String CANCEL_URL = "http://localhost:8080/demo/checkout?action=cancel";
//
//    // 商店資訊
//    public static final String CURRENCY = "TWD";
//    public static final String MERCHANT_NAME = "購物商城測試";
//
//    // 訂單編號前綴
//    public static final String ORDER_PREFIX = "ORDER";
//
//    // 私有建構子，防止實例化
//    private LinePayConfig() {}
//
//    /**
//     * 產生唯一訂單編號
//     */
//    public static String generateOrderNumber() {
//        return ORDER_PREFIX + System.currentTimeMillis();
//    }
//
//    /**
//     * 檢查是否為測試環境
//     */
//    public static boolean isTestEnvironment() {
//        return API_BASE_URL.contains("sandbox");
//    }
//}
