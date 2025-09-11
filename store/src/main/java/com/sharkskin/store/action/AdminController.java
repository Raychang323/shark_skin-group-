package com.sharkskin.store.action;

import com.sharkskin.store.model.Product;
import com.sharkskin.store.model.ProductImage;
import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.OrderRepository;
import com.sharkskin.store.repositories.ProductRepository;
import com.sharkskin.store.repositories.UserRepository;
import com.sharkskin.store.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    public String addProduct(@RequestParam String productId,
                             @RequestParam String name,
                             @RequestParam String price,
                             @RequestParam String stock,
                             @RequestParam(value = "imageUrls", required = false) List<String> imageUrls,
                             HttpSession session,
                             Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        List<String> errors = new ArrayList<>();

        // --- Validation ---
        if (productId == null || productId.trim().isEmpty()) {
            errors.add("商品ID");
        }
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

        // Filter out empty image URLs and check if at least one is provided
        List<String> validImageUrls = imageUrls != null ? imageUrls.stream()
                .filter(url -> url != null && !url.trim().isEmpty())
                .collect(Collectors.toList()) : new ArrayList<>();

        if (validImageUrls.isEmpty()) {
            errors.add("圖片URL(至少一張)");
        }

        // Check if product ID already exists
        if (productRepository.existsById(productId)) {
            errors.add("商品ID已存在");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("message", "資料不完整或不正確：" + String.join(", ", errors));
            // Retain input values
            model.addAttribute("product", new Product(productId, name, parsedPrice, 0)); // Pass partial product for form retention
            // Pass image URLs back for retention
            model.addAttribute("imageUrls", imageUrls); // Pass original list to retain empty fields too
            return "admin_add_product";
        }

        // --- Save Product ---
        Product product = new Product(productId, name, parsedPrice, parsedStock);
        for (String url : validImageUrls) {
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

    @PostMapping("/admin/user-management/delete")
    public String deleteUser(@RequestParam Long userId, HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) {
            return "redirect:/admin/login";
        }

        Optional<UserModel> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserModel userToDelete = userOptional.get();
            // Prevent deleting admin user itself
            if ("admin".equals(userToDelete.getUsername()) && "ADMIN".equals(userToDelete.getRole())) {
                model.addAttribute("message", "無法刪除管理員帳號！");
            } else {
                userRepository.deleteById(userId);
                model.addAttribute("message", "會員刪除成功！");
            }
        } else {
            model.addAttribute("message", "找不到該會員！");
        }
        return "redirect:/admin/user-management";
    }
}