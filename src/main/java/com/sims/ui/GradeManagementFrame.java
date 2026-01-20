package com.sims.ui;

import com.sims.dao.CourseDAO;
import com.sims.dao.GradeDAO;
import com.sims.dao.StudentDAO;
import com.sims.models.Course;
import com.sims.models.Grade;
import com.sims.models.Student;
import com.sims.services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Grade Management Frame
 */
public class GradeManagementFrame extends JPanel {
    private GradeDAO gradeDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private AuthService authService;
    private JComboBox<Student> studentCombo;
    private JComboBox<Course> courseCombo;

    public GradeManagementFrame(AuthService authService) {
        this.authService = authService;
        this.gradeDAO = new GradeDAO();
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Student:"));
        studentCombo = new JComboBox<>();
        try {
            List<Student> students = studentDAO.getAllStudents();
            studentCombo.addItem(null);
            for (Student student : students) {
                studentCombo.addItem(student);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        topPanel.add(studentCombo);

        topPanel.add(new JLabel("Course:"));
        courseCombo = new JComboBox<>();
        try {
            List<Course> courses = courseDAO.getAllCourses();
            courseCombo.addItem(null);
            for (Course course : courses) {
                courseCombo.addItem(course);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        topPanel.add(courseCombo);

        JButton loadButton = new JButton("Load Grades");
        loadButton.addActionListener(e -> loadGrades());
        topPanel.add(loadButton);

        JButton addButton = new JButton("Add Grade");
        addButton.addActionListener(e -> showAddGradeDialog());
        topPanel.add(addButton);

        // Table
        String[] columns = {"ID", "Assessment Type", "Assessment Name", "Marks Obtained", "Total Marks", "Percentage", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gradeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(gradeTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadGrades() {
        Student selectedStudent = (Student) studentCombo.getSelectedItem();
        Course selectedCourse = (Course) courseCombo.getSelectedItem();

        if (selectedStudent == null || selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select both student and course.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(
                    selectedStudent.getStudentId(), selectedCourse.getCourseId());
            for (Grade grade : grades) {
                Object[] row = {
                    grade.getGradeId(),
                    grade.getAssessmentType(),
                    grade.getAssessmentName(),
                    grade.getMarksObtained(),
                    grade.getTotalMarks(),
                    grade.getPercentage() + "%",
                    grade.getGradeLetter()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddGradeDialog() {
        Student selectedStudent = (Student) studentCombo.getSelectedItem();
        Course selectedCourse = (Course) courseCombo.getSelectedItem();

        if (selectedStudent == null || selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select both student and course first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        GradeDialog dialog = new GradeDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                selectedStudent, selectedCourse);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadGrades();
        }
    }

    private class GradeDialog extends JDialog {
        private JTextField typeField, nameField, marksField, totalMarksField, semesterField, yearField;
        private boolean saved = false;
        private Student student;
        private Course course;

        public GradeDialog(JFrame parent, Student student, Course course) {
            super(parent, "Add Grade", true);
            this.student = student;
            this.course = course;
            initializeDialog();
        }

        private void initializeDialog() {
            setSize(400, 350);
            setLocationRelativeTo(getParent());

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            typeField = new JTextField(20);
            nameField = new JTextField(20);
            marksField = new JTextField(20);
            totalMarksField = new JTextField(20);
            semesterField = new JTextField(20);
            yearField = new JTextField(20);

            int row = 0;
            addField(formPanel, gbc, "Assessment Type:", typeField, row++);
            addField(formPanel, gbc, "Assessment Name:", nameField, row++);
            addField(formPanel, gbc, "Marks Obtained:", marksField, row++);
            addField(formPanel, gbc, "Total Marks:", totalMarksField, row++);
            addField(formPanel, gbc, "Semester:", semesterField, row++);
            addField(formPanel, gbc, "Academic Year:", yearField, row++);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveGrade());
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

        private void saveGrade() {
            try {
                Grade grade = new Grade(
                    student.getStudentId(),
                    course.getCourseId(),
                    typeField.getText(),
                    nameField.getText(),
                    new BigDecimal(marksField.getText()),
                    new BigDecimal(totalMarksField.getText()),
                    semesterField.getText(),
                    yearField.getText()
                );
                grade.setRecordedBy(authService.getCurrentUser().getUserId());

                if (gradeDAO.recordGrade(grade)) {
                    JOptionPane.showMessageDialog(this, "Grade recorded successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    dispose();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving grade: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
