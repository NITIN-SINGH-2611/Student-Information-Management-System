package com.sims.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Financial record model representing student financial transactions
 */
public class FinancialRecord {
    private int financialId;
    private int studentId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDate dueDate;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String receiptNumber;
    private Integer recordedBy;

    public enum TransactionType {
        FEE, PAYMENT, REFUND, SCHOLARSHIP, PENALTY
    }

    public enum PaymentStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }

    public FinancialRecord() {}

    public FinancialRecord(int studentId, TransactionType transactionType, BigDecimal amount,
                          String description, LocalDate transactionDate) {
        this.studentId = studentId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.status = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public int getFinancialId() {
        return financialId;
    }

    public void setFinancialId(int financialId) {
        this.financialId = financialId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Integer getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Integer recordedBy) {
        this.recordedBy = recordedBy;
    }

    @Override
    public String toString() {
        return "FinancialRecord{" +
                "financialId=" + financialId +
                ", studentId=" + studentId +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", status=" + status +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
