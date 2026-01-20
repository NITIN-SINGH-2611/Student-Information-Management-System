-- Student Information Management System Database Schema
-- Normalized to 3NF for optimal performance and data integrity

-- Create database
CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

-- Users table for role-based authentication
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Students table
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    enrollment_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_student_code (student_code),
    INDEX idx_status (status),
    INDEX idx_enrollment_date (enrollment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    credits INT NOT NULL,
    instructor_id INT,
    semester VARCHAR(20),
    academic_year VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_code (course_code),
    INDEX idx_instructor (instructor_id),
    INDEX idx_semester_year (semester, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Course enrollments (Many-to-Many relationship)
CREATE TABLE IF NOT EXISTS course_enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('ENROLLED', 'COMPLETED', 'DROPPED') DEFAULT 'ENROLLED',
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, course_id),
    INDEX idx_student (student_id),
    INDEX idx_course (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Attendance table
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') NOT NULL,
    remarks TEXT,
    recorded_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_student_course (student_id, course_id),
    INDEX idx_date (attendance_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Grades table
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    assessment_type VARCHAR(50) NOT NULL,
    assessment_name VARCHAR(100),
    marks_obtained DECIMAL(5,2) NOT NULL,
    total_marks DECIMAL(5,2) NOT NULL,
    percentage DECIMAL(5,2) GENERATED ALWAYS AS ((marks_obtained / total_marks) * 100) STORED,
    grade_letter VARCHAR(2),
    semester VARCHAR(20),
    academic_year VARCHAR(20),
    recorded_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_student_course (student_id, course_id),
    INDEX idx_assessment_type (assessment_type),
    INDEX idx_semester_year (semester, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Financial records table
CREATE TABLE IF NOT EXISTS financial_records (
    financial_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    transaction_type ENUM('FEE', 'PAYMENT', 'REFUND', 'SCHOLARSHIP', 'PENALTY') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    due_date DATE,
    status ENUM('PENDING', 'PAID', 'OVERDUE', 'CANCELLED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date DATE,
    receipt_number VARCHAR(50),
    recorded_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_student (student_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_status (status),
    INDEX idx_transaction_date (transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user
INSERT INTO users (username, password, role, email) VALUES
('admin', 'admin123', 'ADMIN', 'admin@school.edu'),
('teacher', 'teacher123', 'TEACHER', 'teacher@school.edu'),
('student', 'student123', 'STUDENT', 'student@school.edu');

-- Insert sample data for testing
INSERT INTO students (user_id, student_code, first_name, last_name, date_of_birth, gender, email, phone, address, enrollment_date, status) VALUES
(3, 'STU001', 'John', 'Doe', '2000-05-15', 'MALE', 'john.doe@student.edu', '1234567890', '123 Main St, City', '2023-09-01', 'ACTIVE'),
(NULL, 'STU002', 'Jane', 'Smith', '2001-03-20', 'FEMALE', 'jane.smith@student.edu', '0987654321', '456 Oak Ave, City', '2023-09-01', 'ACTIVE');

INSERT INTO courses (course_code, course_name, description, credits, instructor_id, semester, academic_year, status) VALUES
('CS101', 'Introduction to Computer Science', 'Basic programming and computer science concepts', 3, 2, 'FALL', '2023-2024', 'ACTIVE'),
('MATH101', 'Calculus I', 'Differential and integral calculus', 4, 2, 'FALL', '2023-2024', 'ACTIVE'),
('ENG101', 'English Composition', 'Writing and communication skills', 3, 2, 'FALL', '2023-2024', 'ACTIVE');
