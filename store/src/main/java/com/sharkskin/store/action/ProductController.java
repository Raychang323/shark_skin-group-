package com.sharkskin.store.action;

import com.sharkskin.store.model.ProductDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ProductController {

    // Simulate a database of product details
    private static final Map<String, ProductDetail> productDatabase = new HashMap<>();

    static {
        productDatabase.put("p001", new ProductDetail("p001", "鯊魚商品 1", 150, Arrays.asList("https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+1_View1", "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+1_View2"), 10));
        productDatabase.put("p002", new ProductDetail("p002", "鯊魚商品 2", 300, Arrays.asList("https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+2_View1", "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+2_View2"), 5));
        productDatabase.put("p003", new ProductDetail("p003", "鯊魚商品 3", 450, Arrays.asList("https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+3_View1", "https://via.placeholder.com/200/00BFFF/FFFFFF?text=Product+3_View2"), 0));
    }

    @GetMapping("/product/detail/{productId}")
    public String getProductDetail(@PathVariable String productId, Model model) {
        ProductDetail productDetail = productDatabase.get(productId);
        model.addAttribute("product", productDetail);
        return "productDetail";
    }
}
