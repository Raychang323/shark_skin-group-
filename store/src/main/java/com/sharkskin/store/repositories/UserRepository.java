//執行資料庫操作的介面
package com.sharkskin.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharkskin.store.model.UserModel;

@Repository
//JpaRepository(T, ID)，Ｔ=資料表的類別(Model)，ID=Primary Key的型別
public interface UserRepository extends   JpaRepository<UserModel, Long> {
    // 根據使用者名稱查詢
	UserModel findByUsername(String username);
    // 根據 Email 查詢
    UserModel findByEmail(String email);
    // 檢查帳號是否存在
    boolean existsByUsername(String username);
    // 檢查 Email 是否存在
    boolean existsByEmail(String email);
    //@Query(sql語法)可以新增特殊搜尋方法
}