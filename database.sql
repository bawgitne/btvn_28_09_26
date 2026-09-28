-- ========================================================
-- DATABASE SCRIPT FOR IOTSTAR SHOP
-- Spring Boot + Spring Security + MapStruct + Thymeleaf
-- Compatible with Microsoft SQL Server
-- ========================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'shopdb')
BEGIN
    CREATE DATABASE shopdb;
END
GO

USE shopdb;
GO

-- 1. Bảng roles (Lưu danh sách quyền người dùng)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'roles')
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
    );
END
GO

-- 2. Bảng users (Lưu thông tin tài khoản)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username VARCHAR(100) NOT NULL UNIQUE,
        email VARCHAR(150) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        full_name NVARCHAR(200),
        images VARCHAR(500),
        role_id BIGINT NOT NULL,
        enabled BIT NOT NULL DEFAULT 1,
        CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
    );
END
GO

-- 3. Bảng products (Lưu danh sách sản phẩm)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX),
        price DECIMAL(15,2) NOT NULL,
        image_url VARCHAR(500),
        user_id BIGINT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    );
END
GO

-- 4. Bảng otp_tokens (Lưu mã OTP cho đăng ký và quên mật khẩu)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'otp_tokens')
BEGIN
    CREATE TABLE otp_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email VARCHAR(150) NOT NULL,
        otp_hash VARCHAR(100) NOT NULL,
        type VARCHAR(30) NOT NULL, -- REGISTER, RESET_PASSWORD
        expires_at DATETIME2 NOT NULL,
        attempts INT NOT NULL DEFAULT 0,
        used BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX idx_otp_email_type ON otp_tokens(email, type);
END
GO

-- ========================================================
-- DỮ LIỆU KHỞI TẠO MẪU (SEED DATA)
-- ========================================================

-- Thêm Roles mẫu
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_USER');
END
GO

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
END
GO

-- Thêm Tài khoản Admin mẫu: admin / trungnh@hcmute.edu.vn - Mật khẩu: 123456
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, email, password, full_name, images, role_id, enabled)
    VALUES (
        'admin',
        'trungnh@hcmute.edu.vn',
        '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWbys2',
        N'System Administrator',
        '/images/avatar-default.png',
        (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
        1
    );
END
GO

-- Thêm Tài khoản Demo User mẫu (Ví dụ 2): user01 / user01@gmail.com - Mật khẩu: 123456
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'user01')
BEGIN
    INSERT INTO users (username, email, password, full_name, images, role_id, enabled)
    VALUES (
        'user01',
        'user01@gmail.com',
        '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWbys2',
        N'Nguyễn Hữu Trung',
        '/images/user.png',
        (SELECT id FROM roles WHERE name = 'ROLE_USER'),
        1
    );
END
GO

-- Thêm Sản phẩm mẫu cho user01 (Ví dụ 3)
IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Điện thoại Oppo A95')
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Điện thoại Oppo A95',
        N'Màn hình AMOLED 6.43 inch, RAM 8GB, Pin 5000mAh sạc nhanh 33W',
        6500000.00,
        NULL,
        (SELECT id FROM users WHERE username = 'user01'),
        GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Điện thoại Oppo A6')
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Điện thoại Oppo A6',
        N'Thiết kế thời thượng, camera AI sắc nét, hiệu năng ổn định',
        4890000.00,
        NULL,
        (SELECT id FROM users WHERE username = 'user01'),
        GETDATE()
    );
END
GO
