package com.sharkskin.store.action;

import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.service.GcsImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final GcsImageUploadService gcsImageUploadService;

    @Autowired
    public ProductController(ProductRepository productRepository, GcsImageUploadService gcsImageUploadService) {
        this.productRepository = productRepository;
        this.gcsImageUploadService = gcsImageUploadService;
    }

    @GetMapping("/product/detail/{productId}")
    public String getProductDetail(@PathVariable String productId, Model model) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent() && productOptional.get().isListed()) {
            Product product = productOptional.get();

            // Generate signed URLs for each image
            for (ProductImage image : product.getImages()) {
                // The 'imageUrl' field holds the GCS object name (file name)
                String fileName = image.getImageUrl();
                // Generate a signed URL that expires in 60 minutes
                String signedUrl = gcsImageUploadService.generateSignedUrl(fileName, 60);
                // Set the signed URL to the transient field for the view
                image.setSignedUrl(signedUrl);
            }

            model.addAttribute("product", product);
        } else {
            model.addAttribute("product", null); // Product not found or not listed
        }
        return "productDetail";
    }
}