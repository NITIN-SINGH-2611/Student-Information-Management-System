package com.sims.ui;

import com.sims.dao.FinancialDAO;
import com.sims.dao.StudentDAO;
import com.sims.models.FinancialRecord;
import com.sims.models.Student;
import com.sims.services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Financial Management Frame
 */
public class FinancialManagementFrame extends JPanel {
    private FinancialDAO financialDAO;
    private StudentDAO studentDAO;
    private JTable financialTable;
    private DefaultTableModel tableModel;
    private AuthService authService;
    private JComboBox<Student> studentCombo;
    private JLabel balanceLabel;

    public FinancialManagementFrame(AuthService authService) {
        this.authService = authService;
        this.financialDAO = new FinancialDAO();
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
            studentCombo.addActionListener(e -> loadFinancialRecords());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        topPanel.add(studentCombo);

        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(balanceLabel);

        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> showAddTransactionDialog());
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(addButton);

        // Table
        String[] columns = {"ID", "Type", "Amount", "Description", "Date", "Due Date", "Status", "Payment Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        financialTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(financialTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton recordPaymentButton = new JButton("Record Payment");
        recordPaymentButton.addActionListener(e -> showRecordPaymentDialog());
        bottomPanel.add(recordPaymentButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadFinancialRecords() {
        Student selectedStudent = (Student) studentCombo.getSelectedItem();
        if (selectedStudent == null) {
            tableModel.setRowCount(0);
            balanceLabel.setText("Balance: $0.00");
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<FinancialRecord> records = financialDAO.getFinancialRecordsByStudent(selectedStudent.getStudentId());
            for (FinancialRecord record : records) {
                Object[] row = {
                    record.getFinancialId(),
                    record.getTransactionType().name(),
                    "$" + record.getAmount(),
                    record.getDescription(),
                    record.getTransactionDate(),
                    record.getDueDate() != null ? record.getDueDate() : "N/A",
                    record.getStatus().name(),
                    record.getPaymentDate() != null ? record.getPaymentDate() : "N/A"
                };
                tableModel.addRow(row);
            }

            BigDecimal balance = financialDAO.getTotalBalance(selectedStudent.getStudentId());
            balanceLabel.setText("Balance: $" + balance);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading financial records: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddTransactionDialog() {
        Student selectedStudent = (Student) studentCombo.getSelectedItem();
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TransactionDialog dialog = new TransactionDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                selectedStudent);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadFinancialRecords();
        }
    }

    private void showRecordPaymentDialog() {
        int selectedRow = financialTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to record payment.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int financialId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String paymentMethod = JOptionPane.showInputDialog(this, "Enter payment method:",
                "Record Payment", JOptionPane.QUESTION_MESSAGE);
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            try {
                if (financialDAO.updatePaymentStatus(financialId, FinancialRecord.PaymentStatus.PAID,
                        paymentMethod, LocalDate.now(), "REC" + financialId)) {
                    JOptionPane.showMessageDialog(this, "Payment recorded successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadFinancialRecords();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error recording payment: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TransactionDialog extends JDialog {
        private JComboBox<FinancialRecord.TransactionType> typeCombo;
        private JTextField amountField, descriptionField;
        private JSpinner dateSpinner, dueDateSpinner;
        private boolean saved = false;
        private Student student;

        public TransactionDialog(JFrame parent, Student student) {
            super(parent, "Add Transaction", true);
            this.student = student;
            initializeDialog();
        }

        private void initializeDialog() {
            setSize(400, 350);
            setLocationRelativeTo(getParent());

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            typeCombo = new JComboBox<>(FinancialRecord.TransactionType.values());
            amountField = new JTextField(20);
            descriptionField = new JTextField(20);
            dateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
            dateSpinner.setEditor(dateEditor);
            dateSpinner.setValue(java.util.Calendar.getInstance().getTime());

            dueDateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dueDateEditor = new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd");
            dueDateSpinner.setEditor(dueDateEditor);
            dueDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

            int row = 0;
            addField(formPanel, gbc, "Transaction Type:", typeCombo, row++);
            addField(formPanel, gbc, "Amount:", amountField, row++);
            addField(formPanel, gbc, "Description:", descriptionField, row++);
            addField(formPanel, gbc, "Transaction Date:", dateSpinner, row++);
            addField(formPanel, gbc, "Due Date:", dueDateSpinner, row++);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveTransaction());
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

        private void saveTransaction() {
            try {
                FinancialRecord record = new FinancialRecord(
                    student.getStudentId(),
                    (FinancialRecord.TransactionType) typeCombo.getSelectedItem(),
                    new BigDecimal(amountField.getText()),
                    descriptionField.getText(),
                    ((java.util.Date) dateSpinner.getValue()).toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                );
                record.setDueDate(((java.util.Date) dueDateSpinner.getValue()).toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                record.setRecordedBy(authService.getCurrentUser().getUserId());

                if (financialDAO.createFinancialRecord(record)) {
                    JOptionPane.showMessageDialog(this, "Transaction recorded successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    dispose();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
