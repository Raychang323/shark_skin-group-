//邏輯判斷及操作 上：Controller、 下：Repository 
package com.sharkskin.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sharkskin.store.model.UserModel;
import com.sharkskin.store.repositories.UserRepository;

@Service
public class UserService {
	//注入
	  @Autowired
	    private UserRepository userRepository;

	//註冊
	  public boolean register(UserModel user) {
		  //判斷帳號和email是否存在
		  if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
	            return false;
	        }
		  //存進資料庫 明碼存入
	      userRepository.save(user);
		  return true;
		  
	  }
	  //登入
	  public boolean login(String username, String password) {
		  UserModel user = userRepository.findByUsername(username);
			  return (user != null && user.getPassword().equals(password));		  
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
	  //根據帳號抓使用者資料
	  public UserModel getUserByUsername(String username) {
        return userRepository.findByUsername(username);
	    }
	
	
	
}
