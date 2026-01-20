package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course operations
 */
public class CourseDAO {
    
    /**
     * Get all active courses
     */
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_code, course_name, description, credits, " +
                     "instructor_id, semester, academic_year, status " +
                     "FROM courses ORDER BY course_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        return courses;
    }
    
    /**
     * Get course by ID
     */
    public Course getCourseById(int courseId) throws SQLException {
        String sql = "SELECT course_id, course_code, course_name, description, credits, " +
                     "instructor_id, semester, academic_year, status " +
                     "FROM courses WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get courses by student ID (with enrollment join for performance)
     */
    public List<Course> getCoursesByStudentId(int studentId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.description, c.credits, " +
                     "c.instructor_id, c.semester, c.academic_year, c.status " +
                     "FROM courses c " +
                     "INNER JOIN course_enrollments ce ON c.course_id = ce.course_id " +
                     "WHERE ce.student_id = ? AND ce.status = 'ENROLLED' " +
                     "ORDER BY c.course_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }
        return courses;
    }
    
    /**
     * Create new course
     */
    public boolean createCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, description, credits, " +
                     "instructor_id, semester, academic_year, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setString(3, course.getDescription());
            pstmt.setInt(4, course.getCredits());
            pstmt.setObject(5, course.getInstructorId(), java.sql.Types.INTEGER);
            pstmt.setString(6, course.getSemester());
            pstmt.setString(7, course.getAcademicYear());
            pstmt.setString(8, course.getStatus().name());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update course
     */
    public boolean updateCourse(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_name = ?, description = ?, credits = ?, " +
                     "instructor_id = ?, semester = ?, academic_year = ?, status = ? " +
                     "WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getDescription());
            pstmt.setInt(3, course.getCredits());
            pstmt.setObject(4, course.getInstructorId(), java.sql.Types.INTEGER);
            pstmt.setString(5, course.getSemester());
            pstmt.setString(6, course.getAcademicYear());
            pstmt.setString(7, course.getStatus().name());
            pstmt.setInt(8, course.getCourseId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Enroll student in course
     */
    public boolean enrollStudent(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO course_enrollments (student_id, course_id, enrollment_date, status) " +
                     "VALUES (?, ?, CURDATE(), 'ENROLLED') " +
                     "ON DUPLICATE KEY UPDATE status = 'ENROLLED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Map ResultSet to Course object
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setDescription(rs.getString("description"));
        course.setCredits(rs.getInt("credits"));
        
        int instructorId = rs.getInt("instructor_id");
        if (!rs.wasNull()) {
            course.setInstructorId(instructorId);
        }
        
        course.setSemester(rs.getString("semester"));
        course.setAcademicYear(rs.getString("academic_year"));
        course.setStatus(Course.Status.valueOf(rs.getString("status")));
        
        return course;
    }
}
