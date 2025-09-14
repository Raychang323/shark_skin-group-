package com.sharkskin.store.linepay;


import java.util.List;


// LinePayRequest
// 封裝向 Line Pay 發送付款請求時的資料
 
public class LinepayRequest {
	//訂單總金額
    private int totalprice;

    /**
     * 訂單貨幣
     * 例如： "TWD"、"JPY"
     */
    private String currency;

    /**
     * 訂單編號（商家自訂，用於識別訂單）
     */
    private String orderId;

    /**
     * 商品名稱列表
     */
    private List<String> packages;

    /**
     * 付款成功導向的 URL
     * 用戶完成付款後，Line Pay 將導向此 URL
     */
    private String confirmUrl;

    /**
     * 付款失敗或取消導向的 URL
     */
    private String cancelUrl;

    // ------------------------- Constructor -------------------------

    public LinepayRequest () {
    }

    public LinepayRequest(int totalprice, String currency, String orderId, List<String> packages,
                          String confirmUrl, String cancelUrl, String productName) {
        this.totalprice = totalprice;
        this.currency = currency;
        this.orderId = orderId;
        this.packages = packages;
        this.confirmUrl = confirmUrl;
        this.cancelUrl = cancelUrl;
    }

    // ------------------------- Getter / Setter -------------------------

    public int getTotalprice() {
        return totalprice;
    }

    public void setAmount(int amount) {
        this.totalprice = totalprice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = "TWD"; //寫死TWD
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public String getConfirmUrl() {
        return confirmUrl;
    }

    public void setConfirmUrl(String confirmUrl) {
        this.confirmUrl = confirmUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

 

    // ------------------------- 便利方法 -------------------------

    /**
     * 將商品列表轉成 Line Pay API 需要的格式
     * 例如轉成 JSON 或 Map
     */
    // public Map<String, Object> toMap() {...}
}
