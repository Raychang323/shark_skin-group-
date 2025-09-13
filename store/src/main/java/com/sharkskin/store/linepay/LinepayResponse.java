package com.sharkskin.store.linepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * LinePayResponse
 * 用於封裝 Line Pay API 回傳的 JSON 資料。
 * 可以用於付款請求、確認付款或查詢交易結果。
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知欄位，避免 JSON 新增欄位時解析錯誤
public class LinepayResponse {

    /**
     * 回傳結果代碼
     * 0 = 成功
     * 非 0 = 失敗
     */
    @JsonProperty("returnCode")
    private String returnCode;

    /**
     * 回傳訊息
     */
    @JsonProperty("returnMessage")
    private String returnMessage;

    /**
     * 交易資料
     * JSON 內可能包含 paymentUrl、transactionId 等資訊
     */
    @JsonProperty("info")
    private Map<String, Object> info;

    // ====== Getter & Setter ======

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    // ====== 便利方法 ======

    /**
     * 判斷付款請求是否成功
     */
    public boolean isSuccess() {
        return "0000".equals(returnCode); // Line Pay 成功代碼為 0000
    }

    /**
     * 取得付款頁面 URL (若有)
     */
    public String getPaymentUrl() {
        if (info != null && info.containsKey("paymentUrl")) {
            Map<String, String> urls = (Map<String, String>) info.get("paymentUrl");
            return urls.get("web"); // web 或 app
        }
        return null;
    }

    /**
     * 取得交易 ID (transactionId)
     */
    public String getTransactionId() {
        if (info != null && info.containsKey("transactionId")) {
            return String.valueOf(info.get("transactionId"));
        }
        return null;
    }
}

