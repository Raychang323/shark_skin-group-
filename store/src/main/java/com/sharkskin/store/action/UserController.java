package com.sharkskin.store.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.service.UserService;
import com.sharkskin.store.service.CartService; // Add this import

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired // Add this autowired annotation
    private CartService cartService; // Declare cartService

    //到註冊頁面
    @GetMapping("/register")
    public String goRegisterPage() {
        return "register";
    }
    //註冊
    @PostMapping("/register")
    public String register(@RequestParam String username,//前端傳入資料
                           @RequestParam String password,
                           @RequestParam String email,
                           Model model) {
    	UserModel user = new UserModel();
    	user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        //註冊結果
        boolean success = userService.register(user);
        if (success) {
            return "redirect:/verify"; //重導到登入頁面
        } else {
            model.addAttribute("message", "帳號或Email已存在！");
            return "register"; //重回註冊頁面
        }      
    }
    //到登入頁面
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    @PostMapping("/login")  
    public String login(@RequestParam String username,
			            @RequestParam String password,
			            HttpSession session,
			            Model model) {
    	if (userService.login(username, password)) {
            session.setAttribute("username", username);
            cartService.mergeCarts(session); // Call mergeCarts after successful login
            return "redirect:/home";
        }else {
            model.addAttribute("message", "帳號或密碼錯誤！");
            return "login";
        }
    }
    //驗證頁面
    @PostMapping("/verify")
   public String verify(@RequestParam String code,
            @RequestParam String email,
            HttpSession session,
            Model model){
        boolean success = userService.verify(email, code );
        if(success){
            session.setAttribute("email", email);
            return "redirect:/home";
        }else{
            model.addAttribute("message", "驗證失敗");
            return "verify";
        }
            }
    //使用者首頁
    @GetMapping("/home")
    public String home(HttpSession session) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }
        return "home";
    }
    //更新頁面
    @GetMapping("/update")
    public String Update(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        //沒登入
        if (username == null) {
            return "redirect:/login";
        }
        UserModel user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "update";
    }
    @PostMapping("/update")
    public String update(@RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String email,
            HttpSession session,
            Model model) {
    	boolean success = userService.update(username, password, email);
    	 if (success) {
    	            // 更新 session 資料
    	            UserModel updatedUser = userService.getUserByUsername(username);
    	            session.setAttribute("username", updatedUser.getUsername());
    	            session.setAttribute("email", updatedUser.getEmail());
    	            model.addAttribute("message", "更新成功！");
    	            System.out.println("success");
    	 } else {
    	            model.addAttribute("message", "更新失敗！");
    	            System.out.println("false");
    	            System.out.println(success);
    	            System.out.println(username);
    	            System.out.println(email);
    	            System.out.println(password);
    	        }
    	        UserModel user = userService.getUserByUsername(username);
    	        model.addAttribute("user", user);
    	        return "home";
    }
    //登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}