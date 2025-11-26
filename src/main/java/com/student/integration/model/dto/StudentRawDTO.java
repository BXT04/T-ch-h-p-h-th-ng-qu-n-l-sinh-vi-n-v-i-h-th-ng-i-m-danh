package com.student.integration.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO cho dữ liệu sinh viên RAW từ CSV
 * TẤT CẢ fields đều là String để nhận mọi dạng dữ liệu (kể cả sai)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentRawDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Basic Information
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    
    @JsonProperty("gender")
    private String gender;
    
    // Contact Information
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("province")
    private String province;
    
    @JsonProperty("postal_code")
    private String postalCode;
    
    // Academic Information
    @JsonProperty("class_code")
    private String classCode;
    
    @JsonProperty("major")
    private String major;
    
    @JsonProperty("faculty")
    private String faculty;
    
    @JsonProperty("academic_year")
    private String academicYear;
    
    @JsonProperty("enrollment_date")
    private String enrollmentDate;
    
    // Performance
    @JsonProperty("gpa")
    private String gpa;
    
    @JsonProperty("total_credits")
    private String totalCredits;
    
    @JsonProperty("status")
    private String status;
    
    // Metadata
    private String sourceFile;
    private Integer rowNum;
    
    // Constructors
    public StudentRawDTO() {}
    
    // Getters and Setters
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
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
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
    
    public String getClassCode() {
        return classCode;
    }
    
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getFaculty() {
        return faculty;
    }
    
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public String getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public String getGpa() {
        return gpa;
    }
    
    public void setGpa(String gpa) {
        this.gpa = gpa;
    }
    
    public String getTotalCredits() {
        return totalCredits;
    }
    
    public void setTotalCredits(String totalCredits) {
        this.totalCredits = totalCredits;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    public Integer getRowNum() {
        return rowNum;
    }
    
    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }
    
    @Override
    public String toString() {
        return "StudentRawDTO{" +
                "studentId='" + studentId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", sourceFile='" + sourceFile + '\'' +
                ", rowNumber=" + rowNum +
                '}';
    }
}