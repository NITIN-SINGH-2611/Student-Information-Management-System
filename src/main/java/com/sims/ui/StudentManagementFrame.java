package com.sims.ui;

import com.sims.dao.StudentDAO;
import com.sims.models.Student;
import com.sims.services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Student Management Frame for CRUD operations
 */
public class StudentManagementFrame extends JPanel {
    private StudentDAO studentDAO;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private AuthService authService;
    private JTextField searchField;

    public StudentManagementFrame(AuthService authService) {
        this.authService = authService;
        this.studentDAO = new StudentDAO();
        initializeUI();
        loadStudents();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel - Search and Add
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchStudents());
        topPanel.add(searchButton);

        if (authService.isAdmin()) {
            JButton addButton = new JButton("Add Student");
            addButton.addActionListener(e -> showAddStudentDialog());
            topPanel.add(Box.createHorizontalStrut(20));
            topPanel.add(addButton);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> loadStudents());
            topPanel.add(refreshButton);
        }

        // Table
        String[] columns = {"ID", "Student Code", "First Name", "Last Name", "Email", "Phone", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        // Bottom Panel - Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        if (authService.isAdmin()) {
            JButton viewButton = new JButton("View Details");
            viewButton.addActionListener(e -> viewStudentDetails());

            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> showEditStudentDialog());

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteStudent());

            bottomPanel.add(viewButton);
            bottomPanel.add(editButton);
            bottomPanel.add(deleteButton);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadStudents() {
        try {
            tableModel.setRowCount(0);
            List<Student> students = studentDAO.getAllStudents();
            for (Student student : students) {
                Object[] row = {
                    student.getStudentId(),
                    student.getStudentCode(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getStatus().name()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudents() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadStudents();
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<Student> students = studentDAO.searchStudentsByName(searchTerm);
            for (Student student : students) {
                Object[] row = {
                    student.getStudentId(),
                    student.getStudentCode(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getStatus().name()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddStudentDialog() {
        StudentDialog dialog = new StudentDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Student", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadStudents();
        }
    }

    private void showEditStudentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student != null) {
                StudentDialog dialog = new StudentDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Edit Student", student);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadStudents();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading student: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewStudentDetails() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to view.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student != null) {
                String details = String.format(
                    "Student Details:\n\n" +
                    "ID: %d\n" +
                    "Student Code: %s\n" +
                    "Name: %s %s\n" +
                    "Date of Birth: %s\n" +
                    "Gender: %s\n" +
                    "Email: %s\n" +
                    "Phone: %s\n" +
                    "Address: %s\n" +
                    "Enrollment Date: %s\n" +
                    "Status: %s",
                    student.getStudentId(),
                    student.getStudentCode(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getDateOfBirth(),
                    student.getGender(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getAddress(),
                    student.getEnrollmentDate(),
                    student.getStatus()
                );
                JOptionPane.showMessageDialog(this, details, "Student Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading student: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this student?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
            try {
                if (studentDAO.deleteStudent(studentId)) {
                    JOptionPane.showMessageDialog(this, "Student deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete student.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner class for Student Dialog
    private class StudentDialog extends JDialog {
        private JTextField codeField, firstNameField, lastNameField, emailField, phoneField, addressField;
        private JComboBox<Student.Gender> genderCombo;
        private JComboBox<Student.Status> statusCombo;
        private JSpinner dobSpinner, enrollmentSpinner;
        private boolean saved = false;
        private Student student;

        public StudentDialog(JFrame parent, String title, Student student) {
            super(parent, title, true);
            this.student = student;
            initializeDialog();
        }

        private void initializeDialog() {
            setSize(500, 500);
            setLocationRelativeTo(getParent());

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Student Code
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Student Code:"), gbc);
            gbc.gridx = 1;
            codeField = new JTextField(20);
            formPanel.add(codeField, gbc);

            // First Name
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("First Name:"), gbc);
            gbc.gridx = 1;
            firstNameField = new JTextField(20);
            formPanel.add(firstNameField, gbc);

            // Last Name
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Last Name:"), gbc);
            gbc.gridx = 1;
            lastNameField = new JTextField(20);
            formPanel.add(lastNameField, gbc);

            // Date of Birth
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Date of Birth:"), gbc);
            gbc.gridx = 1;
            dobSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dobEditor = new JSpinner.DateEditor(dobSpinner, "yyyy-MM-dd");
            dobSpinner.setEditor(dobEditor);
            formPanel.add(dobSpinner, gbc);

            // Gender
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Gender:"), gbc);
            gbc.gridx = 1;
            genderCombo = new JComboBox<>(Student.Gender.values());
            formPanel.add(genderCombo, gbc);

            // Email
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            emailField = new JTextField(20);
            formPanel.add(emailField, gbc);

            // Phone
            gbc.gridx = 0; gbc.gridy = 6;
            formPanel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            phoneField = new JTextField(20);
            formPanel.add(phoneField, gbc);

            // Address
            gbc.gridx = 0; gbc.gridy = 7;
            formPanel.add(new JLabel("Address:"), gbc);
            gbc.gridx = 1;
            addressField = new JTextField(20);
            formPanel.add(addressField, gbc);

            // Enrollment Date
            gbc.gridx = 0; gbc.gridy = 8;
            formPanel.add(new JLabel("Enrollment Date:"), gbc);
            gbc.gridx = 1;
            enrollmentSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor enrollmentEditor = new JSpinner.DateEditor(enrollmentSpinner, "yyyy-MM-dd");
            enrollmentSpinner.setEditor(enrollmentEditor);
            formPanel.add(enrollmentSpinner, gbc);

            // Status
            gbc.gridx = 0; gbc.gridy = 9;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            statusCombo = new JComboBox<>(Student.Status.values());
            formPanel.add(statusCombo, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveStudent());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            if (student != null) {
                populateFields();
            }

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void populateFields() {
            codeField.setText(student.getStudentCode());
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            emailField.setText(student.getEmail());
            phoneField.setText(student.getPhone());
            addressField.setText(student.getAddress());
            dobSpinner.setValue(java.sql.Date.valueOf(student.getDateOfBirth()));
            enrollmentSpinner.setValue(java.sql.Date.valueOf(student.getEnrollmentDate()));
            genderCombo.setSelectedItem(student.getGender());
            statusCombo.setSelectedItem(student.getStatus());
            codeField.setEditable(false);
        }

        private void saveStudent() {
            try {
                if (student == null) {
                    // Create new student
                    Student newStudent = new Student(
                        codeField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        ((java.util.Date) dobSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        (Student.Gender) genderCombo.getSelectedItem(),
                        emailField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        ((java.util.Date) enrollmentSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    );
                    newStudent.setStatus((Student.Status) statusCombo.getSelectedItem());

                    if (studentDAO.createStudent(newStudent)) {
                        JOptionPane.showMessageDialog(this, "Student added successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                } else {
                    // Update existing student
                    student.setFirstName(firstNameField.getText());
                    student.setLastName(lastNameField.getText());
                    student.setDateOfBirth(((java.util.Date) dobSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                    student.setGender((Student.Gender) genderCombo.getSelectedItem());
                    student.setEmail(emailField.getText());
                    student.setPhone(phoneField.getText());
                    student.setAddress(addressField.getText());
                    student.setEnrollmentDate(((java.util.Date) enrollmentSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                    student.setStatus((Student.Status) statusCombo.getSelectedItem());

                    if (studentDAO.updateStudent(student)) {
                        JOptionPane.showMessageDialog(this, "Student updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving student: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
