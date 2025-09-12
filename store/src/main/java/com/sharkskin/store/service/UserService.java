//邏輯判斷及操作 上：Controller、 下：Repository 
package com.sharkskin.store.service;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        System.out.println("有到達");   
        String code = String.format("%06d", new Random().nextInt(999999));
		user.setVerificationCode(code);
		//判斷帳號和email為空白
		if (user == null|| user.getUsername().isEmpty()||user.getPassword().isEmpty()||user.getEmail().isEmpty()){
	        System.out.println("1");   

			return false;
		}
		//檢查帳號使否存在
		if (userRepository.existsByUsername(user.getUsername())) {
	        System.out.println("2");   

					return false;
				}
		//檢查是否有驗證過的mail存在
		if (userRepository.existsByEmailAndEmailverfyTrue(user.getEmail())) {
	        System.out.println("3");   

				return false;
			}
        System.out.println("4");   

			//存進資料庫 明碼存入
			userRepository.save(user);
			//發送驗證信
			sendEmail(user);
			return true;
	}
	//登入
	public boolean login(String username, String password) {
		UserModel user = userRepository.findByUsername(username);
			return (user != null && user.getPassword().equals(password));		  
	}
	
	//驗證
	public boolean verify (String username, String email, String code){
		System.out.println("驗證抓資料庫"); 
		System.out.println(email); 
		UserModel user=userRepository.findByUsername(username);
		System.out.println(user.getUsername()); 
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
		UserModel user = userRepository.findByUsername(username);
		if(user==null) {
			return false;
		}
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
	//忘記密碼
	//生成resttoken並存入資料庫
	 public boolean restpassword (String username) {
        UserModel user = userRepository.findByUsername(username);
        if(user == null) {
            return false;
        }
		// 隨機 token
        user.setResttoken();
        userRepository.save(user);
        return true;
    }


	//根據帳號抓使用者資料
	public UserModel getUserByUsername(String username) {
		return userRepository.findByUsername(username);
		}
	//驗證成功後刪除同Email的帳號
	@Transactional
    public void deleteUnverifiedUser(String email) {
        int deletedCount = userRepository.deleteNotVerificationMail(email);
        System.out.println("刪除了 " + deletedCount + " 筆未驗證帳號");
    }
		public void sendEmail(UserModel user) {
    SimpleMailMessage message = new SimpleMailMessage();        
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText("您好，感謝您註冊SharkShop，你的驗證碼:\n"+(user.getVerificationCode())); //設置信箱內容
        mailsend.send(message); //發送郵件
     }

     public void forgetpwdEmail(UserModel user, String mes){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText(mes); //設置信箱內容
        mailsend.send(message); //發送郵件
     }
	
	
}
