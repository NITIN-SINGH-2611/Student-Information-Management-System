package com.sims.dao;

import com.sims.database.DatabaseConnection;
import com.sims.models.FinancialRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Financial operations
 * Optimized queries with proper indexing
 */
public class FinancialDAO {
    
    /**
     * Get all financial records for a student
     */
    public List<FinancialRecord> getFinancialRecordsByStudent(int studentId) throws SQLException {
        List<FinancialRecord> records = new ArrayList<>();
        String sql = "SELECT financial_id, student_id, transaction_type, amount, description, " +
                     "transaction_date, due_date, status, payment_method, payment_date, " +
                     "receipt_number, recorded_by " +
                     "FROM financial_records " +
                     "WHERE student_id = ? " +
                     "ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToFinancialRecord(rs));
                }
            }
        }
        return records;
    }
    
    /**
     * Get pending payments for a student
     */
    public List<FinancialRecord> getPendingPayments(int studentId) throws SQLException {
        List<FinancialRecord> records = new ArrayList<>();
        String sql = "SELECT financial_id, student_id, transaction_type, amount, description, " +
                     "transaction_date, due_date, status, payment_method, payment_date, " +
                     "receipt_number, recorded_by " +
                     "FROM financial_records " +
                     "WHERE student_id = ? AND status IN ('PENDING', 'OVERDUE') " +
                     "ORDER BY due_date ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToFinancialRecord(rs));
                }
            }
        }
        return records;
    }
    
    /**
     * Calculate total balance for a student
     * Optimized aggregate query
     */
    public BigDecimal getTotalBalance(int studentId) throws SQLException {
        String sql = "SELECT " +
                     "SUM(CASE WHEN transaction_type IN ('FEE', 'PENALTY') THEN amount ELSE 0 END) - " +
                     "SUM(CASE WHEN transaction_type IN ('PAYMENT', 'REFUND', 'SCHOLARSHIP') THEN amount ELSE 0 END) " +
                     "as balance " +
                     "FROM financial_records " +
                     "WHERE student_id = ? AND status != 'CANCELLED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    return balance != null ? balance : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Create financial record
     */
    public boolean createFinancialRecord(FinancialRecord record) throws SQLException {
        String sql = "INSERT INTO financial_records (student_id, transaction_type, amount, description, " +
                     "transaction_date, due_date, status, payment_method, payment_date, receipt_number, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, record.getStudentId());
            pstmt.setString(2, record.getTransactionType().name());
            pstmt.setBigDecimal(3, record.getAmount());
            pstmt.setString(4, record.getDescription());
            pstmt.setDate(5, java.sql.Date.valueOf(record.getTransactionDate()));
            pstmt.setObject(6, record.getDueDate() != null ? java.sql.Date.valueOf(record.getDueDate()) : null, java.sql.Types.DATE);
            pstmt.setString(7, record.getStatus().name());
            pstmt.setString(8, record.getPaymentMethod());
            pstmt.setObject(9, record.getPaymentDate() != null ? java.sql.Date.valueOf(record.getPaymentDate()) : null, java.sql.Types.DATE);
            pstmt.setString(10, record.getReceiptNumber());
            pstmt.setObject(11, record.getRecordedBy(), java.sql.Types.INTEGER);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int financialId, FinancialRecord.PaymentStatus status, 
                                       String paymentMethod, LocalDate paymentDate, String receiptNumber) throws SQLException {
        String sql = "UPDATE financial_records SET status = ?, payment_method = ?, " +
                     "payment_date = ?, receipt_number = ? " +
                     "WHERE financial_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, paymentMethod);
            pstmt.setObject(3, paymentDate != null ? java.sql.Date.valueOf(paymentDate) : null, java.sql.Types.DATE);
            pstmt.setString(4, receiptNumber);
            pstmt.setInt(5, financialId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Map ResultSet to FinancialRecord object
     */
    private FinancialRecord mapResultSetToFinancialRecord(ResultSet rs) throws SQLException {
        FinancialRecord record = new FinancialRecord();
        record.setFinancialId(rs.getInt("financial_id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setTransactionType(FinancialRecord.TransactionType.valueOf(rs.getString("transaction_type")));
        record.setAmount(rs.getBigDecimal("amount"));
        record.setDescription(rs.getString("description"));
        
        java.sql.Date transactionDate = rs.getDate("transaction_date");
        if (transactionDate != null) {
            record.setTransactionDate(transactionDate.toLocalDate());
        }
        
        java.sql.Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            record.setDueDate(dueDate.toLocalDate());
        }
        
        record.setStatus(FinancialRecord.PaymentStatus.valueOf(rs.getString("status")));
        record.setPaymentMethod(rs.getString("payment_method"));
        
        java.sql.Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            record.setPaymentDate(paymentDate.toLocalDate());
        }
        
        record.setReceiptNumber(rs.getString("receipt_number"));
        
        int recordedBy = rs.getInt("recorded_by");
        if (!rs.wasNull()) {
            record.setRecordedBy(recordedBy);
        }
        
        return record;
    }
}
