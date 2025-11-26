-- ============================================
-- CLEAN DATABASE SCHEMA
-- Chứa dữ liệu đã validate và chuẩn hóa
-- ============================================
DROP DATABASE IF EXISTS student_clean_db;
CREATE DATABASE student_clean_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_clean_db;
-- ===== CHUẨN 3NF =====
-- Bảng Khoa/Ngành (Departments)
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(10) UNIQUE NOT NULL,
    -- VD: IT, BUS, ENG
    department_name VARCHAR(200) NOT NULL,
    -- Công nghệ thông tin
    faculty VARCHAR(200),
    -- Khoa
    established_year INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_department_code (department_code)
) ENGINE = InnoDB;
-- Bảng Lớp học (Classes)
CREATE TABLE classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_code VARCHAR(20) UNIQUE NOT NULL,
    -- VD: IT21A01
    class_name VARCHAR(200) NOT NULL,
    -- CNTT Khóa 2021 - Lớp A01
    department_id INT NOT NULL,
    academic_year VARCHAR(10),
    -- 2021-2025
    total_students INT DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_class_code (class_code),
    INDEX idx_department_id (department_id)
) ENGINE = InnoDB;
-- Bảng Sinh viên CLEAN (đã validate)
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) UNIQUE NOT NULL,
    -- SV20210001 (CHÍNH XÁC)
    full_name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    -- Đã validate format
    phone_number VARCHAR(20) NOT NULL,
    -- Đã chuẩn hóa format
    address TEXT,
    class_id INT NOT NULL,
    department_id INT NOT NULL,
    enrollment_year INT NOT NULL,
    gpa DECIMAL(3, 2) CHECK (
        gpa >= 0.0
        AND gpa <= 4.0
    ),
    credits_completed INT DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'GRADUATED') DEFAULT 'ACTIVE',
    emergency_contact VARCHAR(200),
    emergency_phone VARCHAR(20),
    -- Metadata
    raw_data_id BIGINT,
    -- Reference to raw_students.id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_student_id (student_id),
    INDEX idx_email (email),
    INDEX idx_class_id (class_id),
    INDEX idx_department_id (department_id),
    INDEX idx_status (status),
    INDEX idx_enrollment_year (enrollment_year)
) ENGINE = InnoDB;
-- Bảng Điểm danh CLEAN
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    session_number INT NOT NULL,
    -- Buổi học thứ mấy
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    note TEXT,
    -- Metadata
    raw_data_id BIGINT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_attendance (
        student_id,
        class_id,
        attendance_date,
        session_number
    ),
    INDEX idx_student_id (student_id),
    INDEX idx_class_id (class_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_status (status)
) ENGINE = InnoDB;
-- Bảng lưu lỗi Validation
CREATE TABLE validation_errors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    error_id VARCHAR(100) UNIQUE NOT NULL,
    -- UUID
    source_table VARCHAR(50) NOT NULL,
    -- raw_students, raw_attendance
    source_record_id BIGINT,
    -- ID của record lỗi
    field_name VARCHAR(100),
    -- Trường bị lỗi (email, phone...)
    error_type VARCHAR(50),
    -- INVALID_FORMAT, MISSING_REQUIRED, DUPLICATE...
    error_message TEXT NOT NULL,
    invalid_value TEXT,
    -- Giá trị sai
    batch_id VARCHAR(100),
    -- Batch import nào
    source_file VARCHAR(255),
    row_num INT,
    severity ENUM('ERROR', 'WARNING', 'INFO') DEFAULT 'ERROR',
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP NULL,
    resolution_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_error_id (error_id),
    INDEX idx_source_table (source_table),
    INDEX idx_error_type (error_type),
    INDEX idx_batch_id (batch_id),
    INDEX idx_resolved (resolved),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB;
-- Bảng tracking ETL Process
CREATE TABLE etl_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_id VARCHAR(100) UNIQUE NOT NULL,
    process_type ENUM('EXTRACT', 'VALIDATE', 'TRANSFORM', 'LOAD') NOT NULL,
    status ENUM('RUNNING', 'SUCCESS', 'FAILED') DEFAULT 'RUNNING',
    source_table VARCHAR(50),
    target_table VARCHAR(50),
    records_processed INT DEFAULT 0,
    records_success INT DEFAULT 0,
    records_failed INT DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    duration_seconds INT,
    error_message TEXT,
    INDEX idx_process_id (process_id),
    INDEX idx_process_type (process_type),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at)
) ENGINE = InnoDB;
-- ===== INSERT SAMPLE MASTER DATA =====
-- Insert Departments
INSERT INTO departments (
        department_code,
        department_name,
        faculty,
        established_year,
        description
    )
VALUES (
        'IT',
        'Công nghệ thông tin',
        'Khoa Công nghệ',
        2000,
        'Đào tạo kỹ sư CNTT'
    ),
    (
        'BUS',
        'Quản trị kinh doanh',
        'Khoa Kinh tế',
        1995,
        'Đào tạo cử nhân Quản trị'
    ),
    (
        'ENG',
        'Kỹ thuật điện',
        'Khoa Kỹ thuật',
        1998,
        'Đào tạo kỹ sư Điện'
    ),
    (
        'CIVIL',
        'Xây dựng dân dụng',
        'Khoa Xây dựng',
        1990,
        'Đào tạo kỹ sư Xây dựng'
    ),
    (
        'ECON',
        'Kinh tế',
        'Khoa Kinh tế',
        1992,
        'Đào tạo cử nhân Kinh tế'
    );
-- Insert Classes
INSERT INTO classes (
        class_code,
        class_name,
        department_id,
        academic_year,
        total_students,
        status
    )
VALUES -- IT Classes
    (
        'IT21A01',
        'CNTT Khóa 2021 - Lớp A01',
        1,
        '2021-2025',
        0,
        'ACTIVE'
    ),
    (
        'IT21A02',
        'CNTT Khóa 2021 - Lớp A02',
        1,
        '2021-2025',
        0,
        'ACTIVE'
    ),
    (
        'IT22A01',
        'CNTT Khóa 2022 - Lớp A01',
        1,
        '2022-2026',
        0,
        'ACTIVE'
    ),
    (
        'IT22A02',
        'CNTT Khóa 2022 - Lớp A02',
        1,
        '2022-2026',
        0,
        'ACTIVE'
    ),
    (
        'IT23A01',
        'CNTT Khóa 2023 - Lớp A01',
        1,
        '2023-2027',
        0,
        'ACTIVE'
    ),
    -- Business Classes
    (
        'BU21B01',
        'QTKD Khóa 2021 - Lớp B01',
        2,
        '2021-2025',
        0,
        'ACTIVE'
    ),
    (
        'BU22B01',
        'QTKD Khóa 2022 - Lớp B01',
        2,
        '2022-2026',
        0,
        'ACTIVE'
    ),
    -- Engineering Classes
    (
        'EN21C01',
        'Điện Khóa 2021 - Lớp C01',
        3,
        '2021-2025',
        0,
        'ACTIVE'
    ),
    (
        'EN22C01',
        'Điện Khóa 2022 - Lớp C01',
        3,
        '2022-2026',
        0,
        'ACTIVE'
    ),
    -- Civil Classes
    (
        'CV21D01',
        'Xây dựng Khóa 2021 - Lớp D01',
        4,
        '2021-2025',
        0,
        'ACTIVE'
    );