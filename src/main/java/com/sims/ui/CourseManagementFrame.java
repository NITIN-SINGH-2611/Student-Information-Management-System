package com.sims.ui;

import com.sims.dao.CourseDAO;
import com.sims.dao.StudentDAO;
import com.sims.models.Course;
import com.sims.models.Student;
import com.sims.services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Course Management Frame
 */
public class CourseManagementFrame extends JPanel {
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private AuthService authService;

    public CourseManagementFrame(AuthService authService) {
        this.authService = authService;
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        initializeUI();
        loadCourses();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (authService.isAdmin()) {
            JButton addButton = new JButton("Add Course");
            addButton.addActionListener(e -> showAddCourseDialog());
            topPanel.add(addButton);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> loadCourses());
            topPanel.add(refreshButton);
        }

        // Table
        String[] columns = {"ID", "Course Code", "Course Name", "Credits", "Semester", "Academic Year", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        if (authService.isAdmin()) {
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> showEditCourseDialog());

            JButton enrollButton = new JButton("Enroll Student");
            enrollButton.addActionListener(e -> showEnrollDialog());

            bottomPanel.add(editButton);
            bottomPanel.add(enrollButton);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        try {
            tableModel.setRowCount(0);
            List<Course> courses = courseDAO.getAllCourses();
            for (Course course : courses) {
                Object[] row = {
                    course.getCourseId(),
                    course.getCourseCode(),
                    course.getCourseName(),
                    course.getCredits(),
                    course.getSemester(),
                    course.getAcademicYear(),
                    course.getStatus().name()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddCourseDialog() {
        CourseDialog dialog = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Course", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCourses();
        }
    }

    private void showEditCourseDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            Course course = courseDAO.getCourseById(courseId);
            if (course != null) {
                CourseDialog dialog = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Edit Course", course);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadCourses();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading course: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEnrollDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            List<Student> students = studentDAO.getAllStudents();
            String[] studentNames = students.stream()
                    .map(s -> s.getStudentCode() + " - " + s.getFullName())
                    .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(this,
                    "Select a student to enroll:",
                    "Enroll Student",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    studentNames,
                    studentNames[0]);

            if (selected != null) {
                String studentCode = selected.split(" - ")[0];
                Student student = studentDAO.getStudentByCode(studentCode);
                if (student != null && courseDAO.enrollStudent(student.getStudentId(), courseId)) {
                    JOptionPane.showMessageDialog(this, "Student enrolled successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error enrolling student: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class CourseDialog extends JDialog {
        private JTextField codeField, nameField, descriptionField, creditsField, semesterField, yearField;
        private JComboBox<Course.Status> statusCombo;
        private boolean saved = false;
        private Course course;

        public CourseDialog(JFrame parent, String title, Course course) {
            super(parent, title, true);
            this.course = course;
            initializeDialog();
        }

        private void initializeDialog() {
            setSize(500, 400);
            setLocationRelativeTo(getParent());

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            codeField = new JTextField(20);
            nameField = new JTextField(20);
            descriptionField = new JTextField(20);
            creditsField = new JTextField(20);
            semesterField = new JTextField(20);
            yearField = new JTextField(20);
            statusCombo = new JComboBox<>(Course.Status.values());

            int row = 0;
            addField(formPanel, gbc, "Course Code:", codeField, row++);
            addField(formPanel, gbc, "Course Name:", nameField, row++);
            addField(formPanel, gbc, "Description:", descriptionField, row++);
            addField(formPanel, gbc, "Credits:", creditsField, row++);
            addField(formPanel, gbc, "Semester:", semesterField, row++);
            addField(formPanel, gbc, "Academic Year:", yearField, row++);
            addField(formPanel, gbc, "Status:", statusCombo, row++);

            if (course != null) {
                populateFields();
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveCourse());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            panel.add(field, gbc);
        }

        private void populateFields() {
            codeField.setText(course.getCourseCode());
            nameField.setText(course.getCourseName());
            descriptionField.setText(course.getDescription());
            creditsField.setText(String.valueOf(course.getCredits()));
            semesterField.setText(course.getSemester());
            yearField.setText(course.getAcademicYear());
            statusCombo.setSelectedItem(course.getStatus());
            codeField.setEditable(false);
        }

        private void saveCourse() {
            try {
                if (course == null) {
                    Course newCourse = new Course(
                        codeField.getText(),
                        nameField.getText(),
                        descriptionField.getText(),
                        Integer.parseInt(creditsField.getText()),
                        semesterField.getText(),
                        yearField.getText()
                    );
                    newCourse.setStatus((Course.Status) statusCombo.getSelectedItem());

                    if (courseDAO.createCourse(newCourse)) {
                        JOptionPane.showMessageDialog(this, "Course added successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                } else {
                    course.setCourseName(nameField.getText());
                    course.setDescription(descriptionField.getText());
                    course.setCredits(Integer.parseInt(creditsField.getText()));
                    course.setSemester(semesterField.getText());
                    course.setAcademicYear(yearField.getText());
                    course.setStatus((Course.Status) statusCombo.getSelectedItem());

                    if (courseDAO.updateCourse(course)) {
                        JOptionPane.showMessageDialog(this, "Course updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
