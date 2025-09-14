//邏輯判斷及操作 上：Controller、 下：Repository 
package com.sharkskin.store.service;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.UserRepository;

@Service
public class UserService {
	//注入
	@Autowired
		private UserRepository userRepository;
	@Autowired
    private JavaMailSender mailsend;

	//註冊

	public boolean register(UserModel user) {
        String code = String.format("%06d", new Random().nextInt(999999));
		user.setVerificationCode(code);
		//判斷帳號和email為空白
		if (user == null|| user.getUsername().isEmpty()||user.getPassword().isEmpty()||user.getEmail().isEmpty()){
			return false;
		}
		//檢查帳號使否存在
		if (userRepository.existsByUsername(user.getUsername())) {
					return false;
				}
		//檢查是否有驗證過的mail存在
		if (userRepository.existsByEmailAndEmailverfyFalse(user.getEmail())) {
				return false;
			}
			//存進資料庫 明碼存入
			userRepository.save(user);
			//發送驗證信
			sendEmail(user);
			return true;
	}
	//登入
		public boolean login(String username, String password) {
		Optional<UserModel> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			UserModel user = userOptional.get();
			return user.getPassword().equals(password);
		}
		return false;		  
	}
	public boolean verify (String email, String code){
		UserModel user=userRepository.findByEmail(email);
		if(code.isEmpty()){
			return false;
		}
		if(code.equals(user.getVerificationCode())){
			user.setEmailverfy(true);
			userRepository.save(user);
			return true;
		}
		return false;

	}
	
	//更新
	public boolean update(String username, String newPassword, String newEmail) {
		Optional<UserModel> userOptional = userRepository.findByUsername(username);
		if(userOptional.isEmpty()) {
			return false;
		}
		UserModel user = userOptional.get();
		//更新密碼
		if(newPassword!=null && !newPassword.isEmpty()) {
			user.setPassword(newPassword);
		}

		//更新email
		if (newEmail != null && !newEmail.isEmpty()) {
				user.setEmail(newEmail);
			}
			userRepository.save(user); // 更新資料庫
			return true;
}
	//根據帳號抓使用者資料
	public UserModel getUserByUsername(String username) {
		return userRepository.findByUsername(username).orElse(null);
		}
	


		public void sendEmail(UserModel user) {
    SimpleMailMessage message = new SimpleMailMessage();        
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText("您好，感謝您註冊SharkShop，你的驗證碼:\n"+(user.getVerificationCode())); //設置信箱內容
        mailsend.send(message); //發送郵件
     }

     public void callSendEmail(String username){
        SimpleMailMessage message = new SimpleMailMessage();
		Optional<UserModel> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            // Handle case where user is not found, e.g., log an error or throw an exception
            System.err.println("User not found for sending email: " + username);
            return;
        }
        UserModel user = userOptional.get();
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText("親您好，這是你的驗證碼:\n"+(user.getVerificationCode())); //設置信箱內容
        mailsend.send(message); //發送郵件
     }
	
	
}
