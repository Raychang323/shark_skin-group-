package com.sharkskin.store.action;

import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.service.GcsImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final GcsImageUploadService gcsImageUploadService;

    @Autowired
    public HomeController(ProductRepository productRepository, GcsImageUploadService gcsImageUploadService) {
        this.productRepository = productRepository;
        this.gcsImageUploadService = gcsImageUploadService;
    }

    // Helper method to generate signed URLs for a list of products
    private void generateSignedUrlsForProducts(List<Product> products) {
        for (Product product : products) {
            for (ProductImage image : product.getImages()) {
                String fileName = image.getImageUrl();
                String signedUrl = gcsImageUploadService.generateSignedUrl(fileName, 60); // 60-minute expiration
                image.setSignedUrl(signedUrl);
            }
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> allProducts = productRepository.findAllByListedTrueDistinct();
        Collections.shuffle(allProducts); // Shuffle products for a random order on each load
        generateSignedUrlsForProducts(allProducts); // Generate signed URLs
        model.addAttribute("products", allProducts);
        return "index";
    }

    @GetMapping("/products")
    public String showProductList(Model model,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size,
                                  @RequestParam(name = "name", required = false) String name,
                                  @RequestParam(name = "minPrice", required = false) Integer minPrice,
                                  @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
                                  @RequestParam(name = "sort", defaultValue = "name_asc") String sort) {

        List<Product> allProducts = productRepository.findAllByListedTrueDistinct();
        Stream<Product> productStream = allProducts.stream();

        // Filter by name if provided
        if (name != null && !name.trim().isEmpty()) {
            productStream = productStream.filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()));
        }

        // Filter by min price if provided
        if (minPrice != null && minPrice > 0) {
            productStream = productStream.filter(p -> p.getPrice() >= minPrice);
        }

        // Filter by max price if provided
        if (maxPrice != null && maxPrice > 0) {
            productStream = productStream.filter(p -> p.getPrice() <= maxPrice);
        }

        List<Product> filteredProducts = productStream.collect(Collectors.toList());

        // Sort the filtered list
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    filteredProducts.sort(java.util.Comparator.comparingInt(Product::getPrice));
                    break;
                case "price_desc":
                    filteredProducts.sort(java.util.Comparator.comparingInt(Product::getPrice).reversed());
                    break;
                case "name_desc":
                    filteredProducts.sort(java.util.Comparator.comparing(Product::getName).reversed());
                    break;
                case "name_asc":
                default:
                    filteredProducts.sort(java.util.Comparator.comparing(Product::getName));
                    break;
            }
        }

        // Paginate the sorted list
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());

        List<Product> pageContent = filteredProducts.subList(start, end);
        
        // Generate signed URLs for the paginated content
        generateSignedUrlsForProducts(pageContent);

        Page<Product> productPage = new PageImpl<>(pageContent, pageable, filteredProducts.size());

        // Add page and search params to model
        model.addAttribute("productPage", productPage);
        model.addAttribute("name", name);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "productList";
    }
}
