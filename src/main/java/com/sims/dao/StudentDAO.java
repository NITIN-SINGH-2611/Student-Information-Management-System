package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student operations
 * Optimized queries with proper indexing
 */
public class StudentDAO {
    
    /**
     * Get all students with pagination support
     * Optimized with indexed columns
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, user_id, student_code, first_name, last_name, " +
                     "date_of_birth, gender, email, phone, address, enrollment_date, status " +
                     "FROM students ORDER BY student_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }
    
    /**
     * Get student by ID
     */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, user_id, student_code, first_name, last_name, " +
                     "date_of_birth, gender, email, phone, address, enrollment_date, status " +
                     "FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get student by student code (indexed column for fast lookup)
     */
    public Student getStudentByCode(String studentCode) throws SQLException {
        String sql = "SELECT student_id, user_id, student_code, first_name, last_name, " +
                     "date_of_birth, gender, email, phone, address, enrollment_date, status " +
                     "FROM students WHERE student_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Search students by name (optimized with LIKE and index)
     */
    public List<Student> searchStudentsByName(String searchTerm) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, user_id, student_code, first_name, last_name, " +
                     "date_of_birth, gender, email, phone, address, enrollment_date, status " +
                     "FROM students WHERE first_name LIKE ? OR last_name LIKE ? " +
                     "ORDER BY first_name, last_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        }
        return students;
    }
    
    /**
     * Create new student
     */
    public boolean createStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (user_id, student_code, first_name, last_name, " +
                     "date_of_birth, gender, email, phone, address, enrollment_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, student.getUserId(), java.sql.Types.INTEGER);
            pstmt.setString(2, student.getStudentCode());
            pstmt.setString(3, student.getFirstName());
            pstmt.setString(4, student.getLastName());
            pstmt.setDate(5, java.sql.Date.valueOf(student.getDateOfBirth()));
            pstmt.setString(6, student.getGender().name());
            pstmt.setString(7, student.getEmail());
            pstmt.setString(8, student.getPhone());
            pstmt.setString(9, student.getAddress());
            pstmt.setDate(10, java.sql.Date.valueOf(student.getEnrollmentDate()));
            pstmt.setString(11, student.getStatus().name());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update student information
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                     "gender = ?, email = ?, phone = ?, address = ?, status = ? " +
                     "WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setDate(3, java.sql.Date.valueOf(student.getDateOfBirth()));
            pstmt.setString(4, student.getGender().name());
            pstmt.setString(5, student.getEmail());
            pstmt.setString(6, student.getPhone());
            pstmt.setString(7, student.getAddress());
            pstmt.setString(8, student.getStatus().name());
            pstmt.setInt(9, student.getStudentId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Delete student (cascade will handle related records)
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Map ResultSet to Student object
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            student.setUserId(userId);
        }
        
        student.setStudentCode(rs.getString("student_code"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        
        java.sql.Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            student.setDateOfBirth(dob.toLocalDate());
        }
        
        student.setGender(Student.Gender.valueOf(rs.getString("gender")));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setAddress(rs.getString("address"));
        
        java.sql.Date enrollmentDate = rs.getDate("enrollment_date");
        if (enrollmentDate != null) {
            student.setEnrollmentDate(enrollmentDate.toLocalDate());
        }
        
        student.setStatus(Student.Status.valueOf(rs.getString("status")));
        
        return student;
    }
}
