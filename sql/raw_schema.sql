-- ============================================
-- RAW DATABASE SCHEMA
-- Chứa dữ liệu thô từ CSV (chưa validate)
-- ============================================
DROP DATABASE IF EXISTS student_raw_db;
CREATE DATABASE student_raw_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_raw_db;
-- Bảng sinh viên RAW (nhận mọi dữ liệu, kể cả sai)
CREATE TABLE raw_students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    -- Có thể sai format
    full_name VARCHAR(200),
    -- Có thể null hoặc rỗng
    date_of_birth VARCHAR(50),
    -- Có thể sai format date
    gender VARCHAR(20),
    -- Có thể sai giá trị
    email VARCHAR(200),
    -- Có thể sai format email
    phone_number VARCHAR(50),
    -- Có thể sai format phone
    address TEXT,
    -- Có thể null
    class_code VARCHAR(50),
    -- Có thể sai format
    department_code VARCHAR(50),
    -- Có thể null
    enrollment_year VARCHAR(10),
    -- Có thể sai format
    gpa VARCHAR(20),
    -- Có thể sai format số
    status VARCHAR(50),
    -- Có thể sai enum
    emergency_contact VARCHAR(200),
    -- Có thể null
    emergency_phone VARCHAR(50),
    -- Có thể sai format
    -- Metadata
    source_file VARCHAR(255),
    -- Tên file CSV nguồn
    row_num INT,
    -- Dòng số bao nhiêu trong CSV
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE,
    -- Đã xử lý chưa
    has_errors BOOLEAN DEFAULT FALSE,
    -- Có lỗi không
    INDEX idx_student_id (student_id),
    INDEX idx_processed (processed),
    INDEX idx_imported_at (imported_at)
) ENGINE = InnoDB;
-- Bảng điểm danh RAW
CREATE TABLE raw_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    -- Có thể không tồn tại
    class_code VARCHAR(50),
    -- Có thể null
    attendance_date VARCHAR(50),
    -- Có thể sai format
    session_number VARCHAR(20),
    -- Có thể sai format
    status VARCHAR(50),
    -- Có thể sai enum (PRESENT, ABSENT, LATE, EXCUSED)
    check_in_time VARCHAR(50),
    -- Có thể sai format time
    check_out_time VARCHAR(50),
    -- Có thể sai format time
    note TEXT,
    -- Metadata
    source_file VARCHAR(255),
    row_num INT,
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE,
    has_errors BOOLEAN DEFAULT FALSE,
    INDEX idx_student_id (student_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_processed (processed)
) ENGINE = InnoDB;
-- Bảng lưu batch import
CREATE TABLE import_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    total_rows INT DEFAULT 0,
    successful_rows INT DEFAULT 0,
    failed_rows INT DEFAULT 0,
    status ENUM('PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PROCESSING',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    error_message TEXT,
    INDEX idx_batch_id (batch_id),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at)
) ENGINE = InnoDB;