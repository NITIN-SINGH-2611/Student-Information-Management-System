package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance operations
 * Optimized queries with indexed columns
 */
public class AttendanceDAO {
    
    /**
     * Get attendance by student and course with date range
     * Optimized with composite index on (student_id, course_id, attendance_date)
     */
    public List<Attendance> getAttendanceByStudentAndCourse(int studentId, int courseId, 
                                                              LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, remarks, recorded_by " +
                     "FROM attendance " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "AND attendance_date BETWEEN ? AND ? " +
                     "ORDER BY attendance_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setDate(3, java.sql.Date.valueOf(startDate));
            pstmt.setDate(4, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return attendanceList;
    }
    
    /**
     * Get attendance for a course on a specific date
     */
    public List<Attendance> getAttendanceByCourseAndDate(int courseId, LocalDate date) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, remarks, recorded_by " +
                     "FROM attendance " +
                     "WHERE course_id = ? AND attendance_date = ? " +
                     "ORDER BY student_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, courseId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return attendanceList;
    }
    
    /**
     * Calculate attendance percentage for a student in a course
     * Optimized aggregate query
     */
    public double getAttendancePercentage(int studentId, int courseId) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(CASE WHEN status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(*) as percentage " +
                     "FROM attendance " +
                     "WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("percentage");
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Record attendance (batch insert for multiple students)
     */
    public boolean recordAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status, remarks, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = ?, remarks = ?, recorded_by = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getCourseId());
            pstmt.setDate(3, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            pstmt.setString(4, attendance.getStatus().name());
            pstmt.setString(5, attendance.getRemarks());
            pstmt.setObject(6, attendance.getRecordedBy(), java.sql.Types.INTEGER);
            pstmt.setString(7, attendance.getStatus().name());
            pstmt.setString(8, attendance.getRemarks());
            pstmt.setObject(9, attendance.getRecordedBy(), java.sql.Types.INTEGER);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Batch record attendance for multiple students
     */
    public boolean batchRecordAttendance(List<Attendance> attendanceList) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status, remarks, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = ?, remarks = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Attendance attendance : attendanceList) {
                pstmt.setInt(1, attendance.getStudentId());
                pstmt.setInt(2, attendance.getCourseId());
                pstmt.setDate(3, java.sql.Date.valueOf(attendance.getAttendanceDate()));
                pstmt.setString(4, attendance.getStatus().name());
                pstmt.setString(5, attendance.getRemarks());
                pstmt.setObject(6, attendance.getRecordedBy(), java.sql.Types.INTEGER);
                pstmt.setString(7, attendance.getStatus().name());
                pstmt.setString(8, attendance.getRemarks());
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            
            return results.length > 0;
        } catch (SQLException e) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();
            }
            throw e;
        }
    }
    
    /**
     * Map ResultSet to Attendance object
     */
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setCourseId(rs.getInt("course_id"));
        
        java.sql.Date date = rs.getDate("attendance_date");
        if (date != null) {
            attendance.setAttendanceDate(date.toLocalDate());
        }
        
        attendance.setStatus(Attendance.AttendanceStatus.valueOf(rs.getString("status")));
        attendance.setRemarks(rs.getString("remarks"));
        
        int recordedBy = rs.getInt("recorded_by");
        if (!rs.wasNull()) {
            attendance.setRecordedBy(recordedBy);
        }
        
        return attendance;
    }
}
