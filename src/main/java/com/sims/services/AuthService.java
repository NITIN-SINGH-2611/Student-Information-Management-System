package com.sims.services;

import com.sims.dao.UserDAO;
import com.sims.models.User;

import java.sql.SQLException;

/**
 * Authentication service for role-based access control
 */
public class AuthService {
    private UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user login
     * @param username Username
     * @param password Password
     * @return User object if authenticated, null otherwise
     */
    public User login(String username, String password) {
        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                this.currentUser = user;
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logout current user
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    /**
     * Check if user has teacher role
     */
    public boolean isTeacher() {
        return currentUser != null && currentUser.getRole() == User.Role.TEACHER;
    }

    /**
     * Check if user has student role
     */
    public boolean isStudent() {
        return currentUser != null && currentUser.getRole() == User.Role.STUDENT;
    }

    /**
     * Check if user has permission to access a module
     */
    public boolean hasPermission(String module) {
        if (currentUser == null) return false;

        switch (module) {
            case "STUDENT_MANAGEMENT":
            case "COURSE_MANAGEMENT":
            case "FINANCIAL_MANAGEMENT":
                return isAdmin();
            case "ATTENDANCE":
            case "GRADES":
                return isAdmin() || isTeacher();
            case "VIEW_RECORDS":
                return true; // All roles can view their records
            default:
                return false;
        }
    }
}
