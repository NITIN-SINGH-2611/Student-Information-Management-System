package com.sims;

import com.sims.database.DatabaseConnection;
import com.sims.ui.LoginFrame;

import javax.swing.*;

/**
 * Main entry point for Student Information Management System
 */
public class Main {
    public static void main(String[] args) {
        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database!\n" +
                    "Please ensure MySQL is running and database is set up.\n" +
                    "Check DatabaseConnection.java for connection settings.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Launch login window
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });

        // Register shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
        }));
    }
}
