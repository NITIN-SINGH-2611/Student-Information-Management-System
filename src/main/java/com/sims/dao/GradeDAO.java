package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.Grade;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Grade operations
 * Optimized queries with proper indexing
 */
public class GradeDAO {
    
    /**
     * Get grades by student and course
     */
    public List<Grade> getGradesByStudentAndCourse(int studentId, int courseId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT grade_id, student_id, course_id, assessment_type, assessment_name, " +
                     "marks_obtained, total_marks, percentage, grade_letter, semester, academic_year, " +
                     "recorded_by, created_at " +
                     "FROM grades " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }
        return grades;
    }
    
    /**
     * Get all grades for a student
     */
    public List<Grade> getGradesByStudent(int studentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT grade_id, student_id, course_id, assessment_type, assessment_name, " +
                     "marks_obtained, total_marks, percentage, grade_letter, semester, academic_year, " +
                     "recorded_by, created_at " +
                     "FROM grades " +
                     "WHERE student_id = ? " +
                     "ORDER BY academic_year DESC, semester DESC, created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }
        return grades;
    }
    
    /**
     * Calculate GPA for a student (weighted by course credits)
     * Optimized aggregate query with JOIN
     */
    public BigDecimal calculateGPA(int studentId, String semester, String academicYear) throws SQLException {
        String sql = "SELECT " +
                     "SUM(g.percentage * c.credits) / SUM(c.credits) as gpa " +
                     "FROM grades g " +
                     "INNER JOIN courses c ON g.course_id = c.course_id " +
                     "WHERE g.student_id = ? AND g.semester = ? AND g.academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, semester);
            pstmt.setString(3, academicYear);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal gpa = rs.getBigDecimal("gpa");
                    return gpa != null ? gpa : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Record grade
     */
    public boolean recordGrade(Grade grade) throws SQLException {
        String sql = "INSERT INTO grades (student_id, course_id, assessment_type, assessment_name, " +
                     "marks_obtained, total_marks, grade_letter, semester, academic_year, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getCourseId());
            pstmt.setString(3, grade.getAssessmentType());
            pstmt.setString(4, grade.getAssessmentName());
            pstmt.setBigDecimal(5, grade.getMarksObtained());
            pstmt.setBigDecimal(6, grade.getTotalMarks());
            pstmt.setString(7, grade.getGradeLetter());
            pstmt.setString(8, grade.getSemester());
            pstmt.setString(9, grade.getAcademicYear());
            pstmt.setObject(10, grade.getRecordedBy(), java.sql.Types.INTEGER);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update grade
     */
    public boolean updateGrade(Grade grade) throws SQLException {
        String sql = "UPDATE grades SET assessment_type = ?, assessment_name = ?, " +
                     "marks_obtained = ?, total_marks = ?, grade_letter = ? " +
                     "WHERE grade_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, grade.getAssessmentType());
            pstmt.setString(2, grade.getAssessmentName());
            pstmt.setBigDecimal(3, grade.getMarksObtained());
            pstmt.setBigDecimal(4, grade.getTotalMarks());
            pstmt.setString(5, grade.getGradeLetter());
            pstmt.setInt(6, grade.getGradeId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Map ResultSet to Grade object
     */
    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setStudentId(rs.getInt("student_id"));
        grade.setCourseId(rs.getInt("course_id"));
        grade.setAssessmentType(rs.getString("assessment_type"));
        grade.setAssessmentName(rs.getString("assessment_name"));
        grade.setMarksObtained(rs.getBigDecimal("marks_obtained"));
        grade.setTotalMarks(rs.getBigDecimal("total_marks"));
        grade.setSemester(rs.getString("semester"));
        grade.setAcademicYear(rs.getString("academic_year"));
        
        int recordedBy = rs.getInt("recorded_by");
        if (!rs.wasNull()) {
            grade.setRecordedBy(recordedBy);
        }
        
        java.sql.Date createdAt = rs.getDate("created_at");
        if (createdAt != null) {
            grade.setCreatedAt(createdAt.toLocalDate());
        }
        
        return grade;
    }
}
