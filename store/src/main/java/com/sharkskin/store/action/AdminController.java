package com.sharkskin.store.action;

import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.repositories.UserRepository;
import com.sharkskin.store.service.UserService;
import com.sharkskin.store.service.GcsImageUploadService; // Import GCS service
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

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
    private final GcsImageUploadService gcsImageUploadService; // Inject GCS service

    @Autowired
    public AdminController(UserService userService, ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository, GcsImageUploadService gcsImageUploadService) {
        this.userService = userService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.gcsImageUploadService = gcsImageUploadService;
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
        model.addAttribute("products", products);
        return "admin_product_management";
    }

    // Product Management - Update Stock
    @PostMapping("/admin/product/update-stock")
    public String updateStock(@RequestParam String productId,
                              @RequestParam int stock,
                              HttpSession session) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setStock(stock);
            productRepository.save(product);
        }
        return "redirect:/admin/product-management";
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
}