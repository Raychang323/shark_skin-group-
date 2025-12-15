# 🦈 Shark Skin - 電商平台

一個功能完整的 Java Spring Boot 電商應用，提供商品管理、用戶認證、購物車、訂單管理及線上支付等功能。

## 📋 目錄

- [功能特性](#功能特性)
- [技術棧](#技術棧)
- [專案結構](#專案結構)
- [系統要求](#系統要求)
- [快速開始](#快速開始)
- [環境配置](#環境配置)
- [API 文檔](#api-文檔)
- [貢獻者](#貢獻者)

## ✨ 功能特性

### 用戶管理
- ✅ 用戶註冊與驗證（郵件驗證）
- ✅ 用戶登入與身份驗證
- ✅ 個人資料管理

### 商品管理
- ✅ 商品列表展示與搜尋
- ✅ 商品分類管理
- ✅ 商品詳細資訊頁面
- ✅ 商品圖片上傳（Google Cloud Storage）

### 購物車與訂單
- ✅ 購物車功能（非同步操作）
- ✅ 庫存驗證
- ✅ 訂單建立與管理
- ✅ 訂單查詢與跟蹤

### 支付整合
- ✅ LINE Pay 支付整合
- ✅ 支付確認與回調處理

### 管理員功能
- ✅ 管理員登入與認證（Spring Security）
- ✅ 用戶管理
- ✅ 商品管理（新增、編輯、刪除）
- ✅ 訂單狀態批量更新
- ✅ 管理員儀表板

## 🛠️ 技術棧

| 類別 | 技術 |
|------|------|
| **後端框架** | Spring Boot 3.5.5 |
| **Java 版本** | Java 17+ |
| **數據庫** | MySQL / MariaDB |
| **ORM** | Spring Data JPA / Hibernate |
| **安全認證** | Spring Security |
| **前端模板** | Thymeleaf |
| **雲存儲** | Google Cloud Storage |
| **支付服務** | LINE Pay API |
| **郵件服務** | Gmail SMTP |
| **構建工具** | Maven |
| **打包方式** | WAR |

## 📁 專案結構

```
shark_skin/
├── store/                              # 主應用目錄
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sharkskin/store/
│   │   │   │   ├── action/            # 控制器層
│   │   │   │   ├── config/            # 配置類
│   │   │   │   ├── dto/               # 數據傳輸對象
│   │   │   │   ├── linepay/           # LINE Pay 整合
│   │   │   │   ├── model/             # 實體類 / 模型
│   │   │   │   ├── repositories/      # 數據庫訪問層
│   │   │   │   ├── service/           # 業務邏輯層
│   │   │   │   ├── StoreApplication.java
│   │   │   │   └── ServletInitializer.java
│   │   │   ├── resources/
│   │   │   │   ├── application.properties  # 應用配置
│   │   │   │   ├── static/            # 靜態資源
│   │   │   │   │   ├── css/           # 樣式表
│   │   │   │   │   └── images/        # 圖片資源
│   │   │   │   └── templates/         # Thymeleaf 模板
│   │   │   │       ├── admin_*        # 管理員頁面
│   │   │   │       ├── home.html      # 首頁
│   │   │   │       ├── productList.html
│   │   │   │       ├── productDetail.html
│   │   │   │       ├── cart.html      # 購物車
│   │   │   │       ├── login.html     # 登入
│   │   │   │       ├── register.html  # 註冊
│   │   │   │       └── ...
│   │   └── test/                      # 測試文件
│   ├── pom.xml                        # Maven 依賴配置
│   └── target/                        # 編譯輸出目錄
├── README.md                          # 本文件
├── 開發日誌.md                         # 開發日誌
└── .gitignore
```

## 💻 系統要求

- **JDK 17** 或以上
- **MySQL 5.7** 或 **MariaDB 10.3** 或以上
- **Maven 3.6** 或以上
- **Google Cloud 帳號**（用於 Cloud Storage）
- **LINE Pay 商家帳號**（用於支付功能）

## 🚀 快速開始

### 1. 克隆專案

```bash
git clone https://github.com/Raychang323/shark_skin-group-.git
cd shark_skin/store
```

### 2. 配置資料庫

修改 `src/main/resources/application.properties` 文件中的數據庫連接信息：

```properties
spring.datasource.url=jdbc:mysql://YOUR_HOST:3306/YOUR_DATABASE
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 3. 配置郵件服務（可選）

如果需要啟用郵件驗證功能，配置 Gmail SMTP：

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **注意：** 請使用 Google 應用專用密碼（App Password），而不是 Gmail 密碼。

### 4. 配置 Google Cloud Storage（可選）

如果需要使用圖片上傳功能：

1. 在 Google Cloud 創建一個 service account
2. 下載 JSON 密鑰文件
3. 將密鑰文件放在 `src/main/resources/` 目錄下
4. 在 `application.properties` 配置存儲桶名稱

### 5. 配置 LINE Pay（可選）

在需要支付功能時，在相關配置文件中添加 LINE Pay API 密鑰。

### 6. 構建與運行

```bash
# 使用 Maven 構建
mvn clean install

# 運行應用
mvn spring-boot:run

# 或者使用 Java 直接運行（需要先構建）
java -jar target/store-0.0.1-SNAPSHOT.war
```

應用將在 `http://localhost:8080` 啟動

## ⚙️ 環境配置

### 開發環境配置

1. **IDE 推薦**：IntelliJ IDEA 或 Eclipse
2. **Lombok 支持**：確保 IDE 已安裝 Lombok 插件
3. **JRE 設定**：設定為 Java 17+

### 生產環境注意事項

在上線前，請修改以下設定：

```properties
# 數據庫
spring.jpa.hibernate.ddl-auto=validate  # 改為 validate 或 none

# 安全性
spring.security.user.password=CHANGE_ME  # 修改默認密碼
```

## 📚 API 文檔

### 主要端點

| 端點 | 方法 | 描述 |
|------|------|------|
| `/` | GET | 首頁 |
| `/productList` | GET | 商品列表 |
| `/productDetail/{id}` | GET | 商品詳情 |
| `/register` | GET, POST | 用戶註冊 |
| `/login` | GET, POST | 用戶登入 |
| `/cart` | GET | 購物車 |
| `/order/create` | POST | 建立訂單 |
| `/order/lookup` | GET | 查詢訂單 |
| `/admin/login` | GET, POST | 管理員登入 |
| `/admin/dashboard` | GET | 管理員儀表板 |
| `/admin/product-management` | GET | 商品管理 |
| `/admin/user-management` | GET | 用戶管理 |

> 詳細的 API 文檔將在後續版本中補充

## 🔒 安全性

- ✅ 使用 Spring Security 進行身份驗證與授權
- ✅ 用戶密碼使用加密存儲
- ✅ 郵件驗證確保用戶身份
- ✅ 支付相關敏感信息安全處理

## 📝 開發日誌

查看 [開發日誌.md](開發日誌.md) 了解專案開發歷程。

## 🤝 貢獻者

- **Samuel Liang** - 項目初始化與核心功能開發
- **RayChang** - 用戶認證系統與訂單管理功能
- **ass860528** - 網頁前端UI/UX設計


## 📄 許可證

本專案暫無特定許可證。

## 📞 聯繫方式

如有問題或建議，請通過以下方式聯繫：

- 📧 Email: sharkshop202509@gmail.com
- 🐙 GitHub: [Raychang323/shark_skin-group-](https://github.com/Raychang323/shark_skin-group-)

## 🗺️ 後續開發計劃

- [ ] 完整的 API 文檔與 Swagger 整合
- [ ] 單元測試覆蓋率提升
- [ ] 性能優化與快取機制
- [ ] 移動端應用支持
- [ ] 更多支付方式整合
- [ ] 推薦系統與用戶分析
- [ ] Docker 容器化部署

---

**最後更新：** 2025年12月15日

