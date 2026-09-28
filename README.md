# IOTSTAR SHOP - SPRING BOOT 3 & SPRING SECURITY & MAPSTRUCT & THYMELEAF

Dự án này đã được hợp nhất và hoàn thiện toàn bộ từ **3 tài liệu ví dụ**:
1. **Ví dụ 1**: Đăng nhập cơ bản bằng email qua Spring Security.
2. **Ví dụ 2**: Custom login (đăng nhập bằng Username hoặc Email), hiển thị Fullname và Avatar người dùng trên `header.html` qua Thymeleaf Layout Dialect.
3. **Ví dụ 3**: Hệ thống hoàn chỉnh tích hợp:
   - Đăng ký tài khoản (Register) + gửi mã OTP qua Gmail SMTP để kích hoạt.
   - Quên mật khẩu (Forgot Password) + gửi OTP qua email để reset mật khẩu mới.
   - Quản lý người dùng (CRUD, Search, Pagination, phân quyền ROLE_ADMIN, thống kê user & product).
   - Quản lý sản phẩm (CRUD, Search, Pagination, upload/xóa ảnh trên Cloudinary).
   - Tự động ánh xạ Entity <-> DTO bằng **MapStruct 1.6.3**.
   - Hỗ trợ cả **SQL Server** và **H2 In-Memory Database** (chạy ngay không cần cài đặt SQL Server nếu muốn).

---

## 🚀 CÁCH KHỞI CHẠY DỰ ÁN

### 1. Chạy với Maven
Mở terminal tại thư mục gốc của dự án:

```powershell
mvn spring-boot:run
```

Hoặc nếu dùng wrapper/đường dẫn Maven máy tính:
```powershell
$env:JAVA_HOME="C:\Program Files\Zulu\zulu-25"
& "C:\Users\LENOVO\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" spring-boot:run
```

Truy cập trình duyệt: [http://localhost:8080](http://localhost:8080)

---

## 🔑 TÀI KHOẢN MẪU KHỞI TẠO TỰ ĐỘNG

Khi khởi động lần đầu, hệ thống tự động sinh dữ liệu mẫu:

| Loại tài khoản | Username / Email | Mật khẩu | Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` hoặc `trungnh@hcmute.edu.vn` | `123456` | Toàn quyền quản trị hệ thống, quản lý Users (`/users`), quản lý Products (`/products`), xem Dashboard |
| **User (Demo)** | `user01` hoặc `user01@gmail.com` | `123456` | Người dùng thường, quản lý Products (`/products`), xem thông tin cá nhân |

> Có thể đăng nhập bằng **Username** hoặc **Email** đều được!

---

## ⚙️ CẤU HÌNH BIẾN MÔI TRƯỜNG (.env)

Hệ thống đã hỗ trợ đọc cấu hình từ file `.env`:

### 1. Kết nối SQL Server (nếu sử dụng SQL Server thật)
Mở file `.env` và bỏ comment phần SQL Server:
```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=shopdb;encrypt=false;trustServerCertificate=true;sslProtocol=TLSv1.2;characterEncoding=UTF-8
DB_USERNAME=sa
DB_PASSWORD=your_password
DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### 2. Gửi mã OTP qua Gmail SMTP
Tạo "Mật khẩu ứng dụng" (App Password) trong tài khoản Google và điền vào `.env`:
```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```
*(Nếu chưa cấu hình mail, hệ thống vẫn tự động in mã OTP ra Terminal/Console để bạn kiểm thử ngay lập tức mà không bị lỗi).*

### 3. Tải ảnh lên Cloudinary
Tạo tài khoản trên [cloudinary.com](https://cloudinary.com) và điền vào `.env`:
```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```
*(Nếu chưa điền Cloudinary, hệ thống sẽ tự động chuyển sang lưu ảnh cục bộ trong thư mục `uploads/` để đảm bảo dự án chạy mượt mà).*

---

## 📂 CẤU TRÚC THƯ MỤC DỰ ÁN

```
src/main/java/vn/iotstar/
├── ShopApplication.java            # Class khởi động ứng dụng
├── config/
│   ├── CloudinaryConfig.java       # Cấu hình Bean Cloudinary
│   ├── DataInitializer.java        # Khởi tạo Roles, User mẫu, Product mẫu
│   ├── EncodingConfig.java         # Lọc mã hóa UTF-8
│   └── SecurityConfig.java         # Spring Security 6/7 Filter Chain, Phân quyền
├── controller/
│   ├── AuthController.java         # Đăng nhập, Đăng ký OTP, Quên mật khẩu OTP
│   ├── GlobalErrorController.java  # Xử lý trang lỗi 403, 404, 500
│   ├── HomeController.java         # Trang chủ Dashboard thống kê
│   ├── ProductController.java      # CRUD, Upload ảnh, Phân trang Sản phẩm
│   └── UserController.java         # CRUD, Tìm kiếm, Phân trang Người dùng
├── dto/
│   ├── ForgotPasswordDTO.java
│   ├── LoginDTO.java
│   ├── ProductDTO.java
│   ├── RegisterDTO.java
│   ├── ResetPasswordDTO.java
│   ├── UserDTO.java
│   └── VerifyOtpDTO.java
├── entity/
│   ├── OtpToken.java               # Bảng lưu OTP và hạn dùng
│   ├── Product.java                # Bảng sản phẩm
│   ├── Role.java                   # Bảng vai trò (ROLE_USER, ROLE_ADMIN)
│   └── User.java                   # Bảng người dùng
├── mapper/
│   ├── ProductMapper.java          # MapStruct Mapper Product <-> ProductDTO
│   └── UserMapper.java             # MapStruct Mapper User <-> UserDTO
├── repository/
│   ├── OtpTokenRepository.java
│   ├── ProductRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetails.java      # Lưu id, username, email, fullName, images, role
│   └── CustomUserDetailsService.java # Tìm kiếm người dùng bằng Username hoặc Email
└── service/
    ├── AuthService.java
    ├── CloudinaryService.java
    ├── CloudinaryUploadResult.java
    ├── EmailService.java
    ├── OtpService.java
    ├── ProductService.java
    ├── UserService.java
    └── impl/
        ├── AuthServiceImpl.java
        ├── CloudinaryServiceImpl.java
        ├── EmailServiceImpl.java
        ├── OtpServiceImpl.java
        ├── ProductServiceImpl.java
        └── UserServiceImpl.java

src/main/resources/
├── application.properties
├── static/
│   ├── css/app.css                 # Giao diện hiện đại, responsive
│   └── images/                     # Avatar mặc định và demo
└── templates/
    ├── error.html
    ├── home.html                   # Dashboard
    ├── auth/
    │   ├── forgot-password.html
    │   ├── login.html
    │   ├── register.html
    │   ├── reset-password.html
    │   └── verify-otp.html
    ├── fragments/
    │   ├── footer.html
    │   └── header.html             # Hiển thị Avatar, Họ tên, Role, Đăng xuất
    ├── layouts/
    │   └── layout.html             # Thymeleaf Layout Dialect
    ├── products/
    │   ├── form.html
    │   └── list.html
    └── users/
        ├── form.html
        └── list.html
```
