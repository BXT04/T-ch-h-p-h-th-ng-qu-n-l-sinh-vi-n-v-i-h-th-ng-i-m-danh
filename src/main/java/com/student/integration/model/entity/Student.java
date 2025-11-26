package com.student.integration.model.entity;

import com.student.integration.model.enums.Gender;
import com.student.integration.model.enums.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Entity class cho Student (Clean data)
 * Đại diện cho dữ liệu trong clean database
 */
public class Student {
    
    private Long id;
    
    // Basic Information
    private String studentId;      // SV20210001 (UNIQUE)
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    
    // Contact Information
    private String email;          // UNIQUE
    private String phone;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    
    // Academic Information
    private Integer classId;       // Foreign key to classes table
    private LocalDate enrollmentDate;
    
    // Performance
    private BigDecimal gpa;        // 0.00 - 4.00
    private Integer totalCredits;
    
    // Status
    private StudentStatus status;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Student() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public Integer getClassId() {
        return classId;
    }
    
    public void setClassId(Integer classId) {
        this.classId = classId;
    }
    
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public BigDecimal getGpa() {
        return gpa;
    }
    
    public void setGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }
    
    public Integer getTotalCredits() {
        return totalCredits;
    }
    
    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }
    
    public StudentStatus getStatus() {
        return status;
    }
    
    public void setStatus(StudentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", gpa=" + gpa +
                ", status=" + status +
                '}';
    }
}