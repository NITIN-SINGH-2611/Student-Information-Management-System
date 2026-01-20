package com.sims.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Grade model representing student grades and assessments
 */
public class Grade {
    private int gradeId;
    private int studentId;
    private int courseId;
    private String assessmentType;
    private String assessmentName;
    private BigDecimal marksObtained;
    private BigDecimal totalMarks;
    private BigDecimal percentage;
    private String gradeLetter;
    private String semester;
    private String academicYear;
    private Integer recordedBy;
    private LocalDate createdAt;

    public Grade() {}

    public Grade(int studentId, int courseId, String assessmentType, String assessmentName,
                 BigDecimal marksObtained, BigDecimal totalMarks, String semester, String academicYear) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.assessmentType = assessmentType;
        this.assessmentName = assessmentName;
        this.marksObtained = marksObtained;
        this.totalMarks = totalMarks;
        this.semester = semester;
        this.academicYear = academicYear;
        calculatePercentageAndGrade();
    }

    private void calculatePercentageAndGrade() {
        if (totalMarks != null && totalMarks.compareTo(BigDecimal.ZERO) > 0 && marksObtained != null) {
            this.percentage = marksObtained.divide(totalMarks, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            this.gradeLetter = calculateGradeLetter(this.percentage);
        }
    }

    private String calculateGradeLetter(BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("90")) >= 0) return "A+";
        if (percentage.compareTo(new BigDecimal("80")) >= 0) return "A";
        if (percentage.compareTo(new BigDecimal("70")) >= 0) return "B+";
        if (percentage.compareTo(new BigDecimal("60")) >= 0) return "B";
        if (percentage.compareTo(new BigDecimal("50")) >= 0) return "C+";
        if (percentage.compareTo(new BigDecimal("40")) >= 0) return "C";
        return "F";
    }

    // Getters and Setters
    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getAssessmentName() {
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
        calculatePercentageAndGrade();
    }

    public BigDecimal getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(BigDecimal totalMarks) {
        this.totalMarks = totalMarks;
        calculatePercentageAndGrade();
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public String getGradeLetter() {
        return gradeLetter;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Integer recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", assessmentType='" + assessmentType + '\'' +
                ", marksObtained=" + marksObtained +
                ", totalMarks=" + totalMarks +
                ", percentage=" + percentage +
                ", gradeLetter='" + gradeLetter + '\'' +
                '}';
    }
}
