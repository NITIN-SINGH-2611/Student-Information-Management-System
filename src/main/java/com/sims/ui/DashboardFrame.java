package com.sims.ui;

import com.sims.models.User;
import com.sims.services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main dashboard window with role-based menu access
 */
public class DashboardFrame extends JFrame {
    private AuthService authService;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public DashboardFrame(AuthService authService) {
        this.authService = authService;
        initializeComponents();
        setupLayout();
        setupMenu();
    }

    private void initializeComponents() {
        setTitle("Student Information Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Welcome panel
        User currentUser = authService.getCurrentUser();
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        welcomePanel.add(new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")"));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authService.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
        welcomePanel.add(Box.createHorizontalGlue());
        welcomePanel.add(logoutButton);

        add(welcomePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Show default view
        showDefaultView();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Student Management Menu
        if (authService.isAdmin()) {
            JMenu studentMenu = new JMenu("Student Management");
            JMenuItem viewStudents = new JMenuItem("View Students");
            viewStudents.addActionListener(e -> showStudentManagement());
            studentMenu.add(viewStudents);
            menuBar.add(studentMenu);
        }

        // Course Management Menu
        if (authService.isAdmin() || authService.isTeacher()) {
            JMenu courseMenu = new JMenu("Course Management");
            JMenuItem viewCourses = new JMenuItem("View Courses");
            viewCourses.addActionListener(e -> showCourseManagement());
            courseMenu.add(viewCourses);
            menuBar.add(courseMenu);
        }

        // Attendance Menu
        if (authService.isAdmin() || authService.isTeacher()) {
            JMenu attendanceMenu = new JMenu("Attendance");
            JMenuItem recordAttendance = new JMenuItem("Record Attendance");
            recordAttendance.addActionListener(e -> showAttendanceManagement());
            attendanceMenu.add(recordAttendance);
            menuBar.add(attendanceMenu);
        }

        // Grades Menu
        if (authService.isAdmin() || authService.isTeacher()) {
            JMenu gradesMenu = new JMenu("Grades");
            JMenuItem manageGrades = new JMenuItem("Manage Grades");
            manageGrades.addActionListener(e -> showGradeManagement());
            gradesMenu.add(manageGrades);
            menuBar.add(gradesMenu);
        }

        // Financial Menu
        if (authService.isAdmin()) {
            JMenu financialMenu = new JMenu("Financial");
            JMenuItem manageFinancial = new JMenuItem("Financial Records");
            manageFinancial.addActionListener(e -> showFinancialManagement());
            financialMenu.add(manageFinancial);
            menuBar.add(financialMenu);
        }

        setJMenuBar(menuBar);
    }

    private void showDefaultView() {
        JPanel defaultPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel(
            "<html><div style='text-align: center; padding: 50px;'>" +
            "<h1>Welcome to Student Information Management System</h1>" +
            "<p>Use the menu above to navigate to different modules.</p>" +
            "<p>Your role: " + authService.getCurrentUser().getRole() + "</p>" +
            "</div></html>",
            JLabel.CENTER
        );
        defaultPanel.add(welcomeLabel, BorderLayout.CENTER);
        contentPanel.add(defaultPanel, "DEFAULT");
        cardLayout.show(contentPanel, "DEFAULT");
    }

    private void showStudentManagement() {
        StudentManagementFrame studentFrame = new StudentManagementFrame(authService);
        contentPanel.add(studentFrame, "STUDENTS");
        cardLayout.show(contentPanel, "STUDENTS");
    }

    private void showCourseManagement() {
        CourseManagementFrame courseFrame = new CourseManagementFrame(authService);
        contentPanel.add(courseFrame, "COURSES");
        cardLayout.show(contentPanel, "COURSES");
    }

    private void showAttendanceManagement() {
        AttendanceManagementFrame attendanceFrame = new AttendanceManagementFrame(authService);
        contentPanel.add(attendanceFrame, "ATTENDANCE");
        cardLayout.show(contentPanel, "ATTENDANCE");
    }

    private void showGradeManagement() {
        GradeManagementFrame gradeFrame = new GradeManagementFrame(authService);
        contentPanel.add(gradeFrame, "GRADES");
        cardLayout.show(contentPanel, "GRADES");
    }

    private void showFinancialManagement() {
        FinancialManagementFrame financialFrame = new FinancialManagementFrame(authService);
        contentPanel.add(financialFrame, "FINANCIAL");
        cardLayout.show(contentPanel, "FINANCIAL");
    }
}
