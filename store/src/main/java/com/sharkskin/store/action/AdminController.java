package com.sharkskin.store.action;

import com.sharkskin.store.model.Order;
import com.sharkskin.store.model.OrderStatus;
import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.repositories.UserRepository;
import com.sharkskin.store.service.OrderService;
import com.sharkskin.store.service.UserService;
import com.sharkskin.store.service.GcsImageUploadService; // Import GCS service
import com.sharkskin.store.repositories.ProductImageRepository; // Import ProductImageRepository
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException; // Import IOException
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserService userService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final GcsImageUploadService gcsImageUploadService; // Inject GCS service
    private final ProductImageRepository productImageRepository; // Inject ProductImageRepository

    @Autowired
    public AdminController(UserService userService, ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository, OrderService orderService, GcsImageUploadService gcsImageUploadService, ProductImageRepository productImageRepository) {
        this.userService = userService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.gcsImageUploadService = gcsImageUploadService;
        this.productImageRepository = productImageRepository;
    }

    // Admin Login
    @GetMapping("/admin/login")
    public String showAdminLoginPage() {
        return "admin_login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        UserModel user = userService.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
            session.setAttribute("adminUsername", username);
            return "redirect:/portal/a9x3z7/dashboard";
        } else {
            model.addAttribute("message", "帳號或密碼錯誤，或您沒有管理員權限！");
            return "admin_login";
        }
    }

    // Admin Dashboard
    @GetMapping("/portal/a9x3z7/dashboard")
    public String showAdminDashboard(HttpSession session) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login"; // Not logged in as admin, redirect to admin login
        }
        return "admin_dashboard";
    }

    // Admin Logout
    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate(); // Invalidate the admin session
        return "redirect:/admin/login";
    }

    // Product Management - Add Product
    @GetMapping("/admin/add-product")
    public String showAddProductForm(HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }
        return "admin_add_product";
    }

    @PostMapping("/admin/add-product")
    public String addProduct(@RequestParam String name,
                             @RequestParam String price,
                             @RequestParam String stock,
                             @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles, // Change to MultipartFile
                             HttpSession session,
                             Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        List<String> errors = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>(); // To store uploaded image URLs

        // --- Validation ---
        // Removed productId validation
        if (name == null || name.trim().isEmpty()) {
            errors.add("商品名稱");
        }

        int parsedPrice = -1;
        try {
            parsedPrice = Integer.parseInt(price);
            if (parsedPrice < 0) errors.add("價格(需為非負數)");
        } catch (NumberFormatException e) {
            errors.add("價格(格式不正確)");
        }

        int parsedStock = -1;
        try {
            parsedStock = Integer.parseInt(stock);
            if (parsedStock < 0) errors.add("庫存(需為非負數)");
        } catch (NumberFormatException e) {
            errors.add("庫存(格式不正確)");
        }

        // Handle image uploads
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String imageUrl = gcsImageUploadService.uploadFile(file);
                        imageUrls.add(imageUrl);
                    } catch (IOException e) {
                        errors.add("圖片上傳失敗: " + file.getOriginalFilename());
                        // Log the error e.printStackTrace();
                    }
                }
            }
        }

        if (imageUrls.isEmpty()) { // Check if at least one image was successfully uploaded
            errors.add("圖片(至少一張)");
        }

        // Check if product ID already exists
        // This check is now removed as ID is generated

        if (!errors.isEmpty()) {
            model.addAttribute("message", "資料不完整或不正確：" + String.join(", ", errors));
            // Retain input values
            model.addAttribute("product", new Product(UUID.randomUUID().toString(), name, parsedPrice, 0)); // Pass partial product for form retention
            // Pass image URLs back for retention
            model.addAttribute("imageUrls", imageUrls); // Pass original list to retain empty fields too
            return "admin_add_product";
        }

        // --- Save Product ---
        String productId = UUID.randomUUID().toString(); // Generate new product ID
        Product product = new Product(productId, name, parsedPrice, parsedStock);
        for (String url : imageUrls) { // Use uploaded URLs
            product.addImage(new ProductImage(url, product));
        }

        productRepository.save(product);

        model.addAttribute("successMessage", "商品上架成功！");
        return "admin_upload_success";
    }

    // User Management
    @GetMapping("/admin/user-management")
    public String showUserManagement(HttpSession session, Model model,
                                     @RequestParam(name = "sort", defaultValue = "id_asc") String sort) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        List<UserModel> users = userRepository.findAll();
        List<String> usersWithOrders = orderRepository.findAll().stream()
                .map(order -> order.getEmail())
                .distinct()
                .collect(Collectors.toList());

        // Set hasOrders flag for each user
        users.forEach(user -> {
            user.setHasOrders(usersWithOrders.contains(user.getEmail()));
        });

        // Apply sorting
        switch (sort) {
            case "id_asc":
                users.sort(Comparator.comparing(UserModel::getId));
                break;
            case "id_desc":
                users.sort(Comparator.comparing(UserModel::getId).reversed());
                break;
            case "name_asc":
                users.sort(Comparator.comparing(UserModel::getUsername));
                break;
            case "name_desc":
                users.sort(Comparator.comparing(UserModel::getUsername).reversed());
                break;
            case "has_orders_asc":
                users.sort(Comparator.comparing(UserModel::getHasOrders));
                break;
            case "has_orders_desc":
                users.sort(Comparator.comparing(UserModel::getHasOrders).reversed());
                break;
            default:
                users.sort(Comparator.comparing(UserModel::getId)); // Default sort
                break;
        }

        model.addAttribute("users", users);
        model.addAttribute("sort", sort);
        return "admin_user_management";
    }

    // Product Management - List Products
    @GetMapping("/admin/product-management")
    public String showProductManagement(HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }
        List<Product> products = productRepository.findAllDistinct();
        // Generate signed URLs for each product image
        for (Product product : products) {
            if (product.getImages() != null) {
                for (ProductImage image : product.getImages()) {
                    String signedUrl = gcsImageUploadService.generateSignedUrl(image.getImageUrl(), 60); // 60-minute expiration
                    image.setSignedUrl(signedUrl);
                }
            }
        }
        model.addAttribute("products", products);
        return "admin_product_management";
    }


    // Product Management - Toggle Product Listed Status
    @PostMapping("/admin/product/toggle-listed")
    public String toggleProductListedStatus(@RequestParam String productId, HttpSession session) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setListed(!product.isListed());
            productRepository.save(product);
        }
        return "redirect:/admin/product-management";
    }

    // Product Management - Show Edit Product Form
    @GetMapping("/admin/product/edit/{productId}")
    public String showEditProductForm(@PathVariable String productId, HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            // Product not found, redirect to product management with an error
            return "redirect:/admin/product-management"; // Or a dedicated error page
        }

        Product product = productOptional.get();

        // Generate signed URLs for each product image
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                String signedUrl = gcsImageUploadService.generateSignedUrl(image.getImageUrl(), 60); // 60-minute expiration
                image.setSignedUrl(signedUrl);
            }
        }

        model.addAttribute("product", product);
        return "admin_edit_product";
    }

    // Product Management - Update Product
    @PostMapping("/admin/product/update")
    public String updateProduct(@RequestParam String productId,
                                @RequestParam String name,
                                @RequestParam String price,
                                @RequestParam String stock,
                                @RequestParam(value = "imagesToDelete", required = false) List<Long> imagesToDelete,
                                @RequestParam(value = "newImageFiles", required = false) List<MultipartFile> newImageFiles,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "商品未找到！");
            return "redirect:/admin/product-management";
        }

        Product product = productOptional.get();

        // --- Update basic product details ---
        product.setName(name);
        try {
            product.setPrice(Integer.parseInt(price));
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("message", "價格格式不正確！");
            return "redirect:/admin/product/edit/" + productId;
        }
        try {
            product.setStock(Integer.parseInt(stock));
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("message", "庫存格式不正確！");
            return "redirect:/admin/product/edit/" + productId;
        }

        // --- Handle image deletion ---
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            List<ProductImage> imagesToRemove = new ArrayList<>();
            for (Long imageId : imagesToDelete) {
                Optional<ProductImage> imgOptional = productImageRepository.findById(imageId);
                if (imgOptional.isPresent()) {
                    ProductImage img = imgOptional.get();
                    // Delete from GCS
                    gcsImageUploadService.deleteFile(img.getImageUrl());
                    imagesToRemove.add(img);
                }
            }
            // Remove from product's image list and database
            product.getImages().removeAll(imagesToRemove);
            productImageRepository.deleteAll(imagesToRemove);
        }

        // --- Handle new image uploads ---
        int currentImageCount = product.getImages() != null ? product.getImages().size() : 0;
        final int MAX_IMAGES = 10;

        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile file : newImageFiles) {
                if (!file.isEmpty()) {
                    if (currentImageCount >= MAX_IMAGES) {
                        redirectAttributes.addFlashAttribute("message", "圖片數量已達上限（" + MAX_IMAGES + "張）！");
                        break; // Stop processing new files
                    }
                    try {
                        String imageUrl = gcsImageUploadService.uploadFile(file);
                        product.addImage(new ProductImage(imageUrl, product));
                        currentImageCount++;
                    } catch (IOException e) {
                        redirectAttributes.addFlashAttribute("message", "圖片上傳失敗: " + file.getOriginalFilename());
                        // Log the error e.printStackTrace();
                    }
                }
            }
        }

        // --- Save updated product ---
        productRepository.save(product);

        redirectAttributes.addFlashAttribute("successMessage", "商品更新成功！");
        return "redirect:/admin/product-management";
    }
}
