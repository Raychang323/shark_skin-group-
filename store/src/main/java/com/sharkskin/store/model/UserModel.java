package com.sharkskin.store.model;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity //標記JPA實體類別
@Table(name = "user") //標記資料庫table name
public class UserModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //流水id
    @Column(name = "user_name", unique = true, nullable = false)
    private String username;
	@Column(name = "password", nullable = false)
	private String password; //密碼
	@Column(name = "email", nullable = false)
	private String email;  //email
	@Column(name = "role")
	private String role = "USER"; //身份寫死為USER
	@CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createdtime; //註冊時間 自動紀錄
	@CreationTimestamp
    @Column(name = "update_time", nullable = false, updatable = true)
    private LocalDateTime updatetime; //更新資料時間 自動紀錄
	@Column(name = "verification_code")
	private String verificationCode; //紀錄驗證碼
	@Column(name = "emailverfy", nullable = false)
	private boolean emailverfy =false; //email驗證 預設為false
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Instant getCreatedtime() {
		return createdtime;
	}
	public void setCreatedtime(Instant createdtime) {
		this.createdtime = createdtime;
	}
	public LocalDateTime getUpdatetime(){
		return updatetime;
	}
	public void setUpdatetime(Instant updatetime){
        this.updatetime = LocalDateTime.now();
	}
	public String getVerificationCode (){
		return verificationCode;
	}
	public void setVerificationCode(String verificationCode){
		this.verificationCode=verificationCode;
	}
	
	public boolean getEmailverfy (){
		return emailverfy;
	}
	public void setEmailverfy (boolean emailverfy){
		this.emailverfy=emailverfy;
	}
	
	
	
}
