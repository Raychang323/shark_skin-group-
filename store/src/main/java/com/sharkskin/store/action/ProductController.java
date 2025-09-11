package com.sharkskin.store.action;

import com.sharkskin.store.model.Product;
import com.sharkskin.store.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/detail/{productId}")
    public String getProductDetail(@PathVariable String productId, Model model) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get());
        } else {
            model.addAttribute("product", null); // Product not found
        }
        return "productDetail";
    }
}