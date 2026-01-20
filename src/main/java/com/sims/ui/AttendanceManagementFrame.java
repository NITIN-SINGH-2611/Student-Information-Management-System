package com.sims.ui;

import com.sims.dao.AttendanceDAO;
import com.sims.dao.CourseDAO;
import com.sims.dao.StudentDAO;
import com.sims.models.Attendance;
import com.sims.models.Course;
import com.sims.models.Student;
import com.sims.services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Attendance Management Frame
 */
public class AttendanceManagementFrame extends JPanel {
    private AttendanceDAO attendanceDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private AuthService authService;
    private JComboBox<Course> courseCombo;
    private JSpinner dateSpinner;

    public AttendanceManagementFrame(AuthService authService) {
        this.authService = authService;
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Course:"));
        courseCombo = new JComboBox<>();
        try {
            List<Course> courses = courseDAO.getAllCourses();
            for (Course course : courses) {
                courseCombo.addItem(course);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        topPanel.add(courseCombo);
        topPanel.add(new JLabel("Date:"));
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        topPanel.add(dateSpinner);

        JButton loadButton = new JButton("Load Students");
        loadButton.addActionListener(e -> loadStudentsForAttendance());
        topPanel.add(loadButton);

        JButton saveButton = new JButton("Save Attendance");
        saveButton.addActionListener(e -> saveAttendance());
        topPanel.add(saveButton);

        // Table
        String[] columns = {"Student Code", "Student Name", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return String.class;
                return super.getColumnClass(column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.getColumn("Status").setCellEditor(new DefaultCellEditor(new JComboBox<>(
                new String[]{"PRESENT", "ABSENT", "LATE", "EXCUSED"})));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadStudentsForAttendance() {
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<Student> students = studentDAO.getAllStudents();
            for (Student student : students) {
                Object[] row = {
                    student.getStudentCode(),
                    student.getFullName(),
                    "PRESENT"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttendance() {
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        try {
            List<Attendance> attendanceList = new java.util.ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String studentCode = (String) tableModel.getValueAt(i, 0);
                String status = (String) tableModel.getValueAt(i, 2);

                Student student = studentDAO.getStudentByCode(studentCode);
                if (student != null) {
                    Attendance attendance = new Attendance(
                        student.getStudentId(),
                        selectedCourse.getCourseId(),
                        date,
                        Attendance.AttendanceStatus.valueOf(status)
                    );
                    attendance.setRecordedBy(authService.getCurrentUser().getUserId());
                    attendanceList.add(attendance);
                }
            }

            if (attendanceDAO.batchRecordAttendance(attendanceList)) {
                JOptionPane.showMessageDialog(this, "Attendance recorded successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
