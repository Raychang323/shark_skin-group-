package com.sharkskin.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.UserRepository;

public class EmailSend {

    @Autowired
    private JavaMailSender mailsend;
    private UserRepository userRepository; 
    public void sendEmail(UserModel user) {
    SimpleMailMessage message = new SimpleMailMessage();        
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText("您好，感謝您註冊SharkShop，你的驗證碼:\n"+(user.getVerificationCode())); //設置信箱內容
        mailsend.send(message); //發送郵件
     }

     public void callSendEmail(String username){
        SimpleMailMessage message = new SimpleMailMessage();
		UserModel user = userRepository.findByUsername(username);
        message.setTo(user.getEmail()); //設置收件人信箱
        message.setSubject("鯊皮認證"); //設置信箱主題
        message.setText("親您好，這是你的驗證碼:\n"+(user.getVerificationCode())); //設置信箱內容
        mailsend.send(message); //發送郵件
     }
}
