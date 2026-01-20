package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User operations
 * Uses prepared statements for security and performance
 */
public class UserDAO {
    
    /**
     * Authenticate user by username and password
     * Optimized query with indexed username column
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, password, role, email FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        }
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password, role, email FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        }
        return null;
    }
    
    /**
     * Create new user
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getEmail());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
