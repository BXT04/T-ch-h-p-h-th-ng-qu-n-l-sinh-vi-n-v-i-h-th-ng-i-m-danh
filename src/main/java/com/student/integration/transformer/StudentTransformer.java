package com.student.integration.transformer;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.entity.Student;
import com.student.integration.model.enums.Gender;
import com.student.integration.model.enums.StudentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Transformer để convert StudentRawDTO (String) → Student Entity (proper types)
 */
public class StudentTransformer {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentTransformer.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    /**
     * Transform StudentRawDTO to Student Entity
     * 
     * @param rawDTO Raw data (all strings)
     * @param classId Class ID từ database lookup
     * @return Student entity với proper types
     */
    public Student transform(StudentRawDTO rawDTO, Integer classId) {
        Student student = new Student();
        
        try {
            // Basic Information
            student.setStudentId(rawDTO.getStudentId());
            student.setFullName(rawDTO.getFullName());
            
            // Date of Birth (String → LocalDate)
            if (rawDTO.getDateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(rawDTO.getDateOfBirth(), DATE_FORMATTER));
            }
            
            // Gender (String → Enum)
            if (rawDTO.getGender() != null) {
                student.setGender(Gender.fromString(rawDTO.getGender()));
            }
            
            // Contact Information
            student.setEmail(rawDTO.getEmail());
            student.setPhone(rawDTO.getPhone());
            student.setAddress(rawDTO.getAddress());
            student.setCity(rawDTO.getCity());
            student.setProvince(rawDTO.getProvince());
            student.setPostalCode(rawDTO.getPostalCode());
            
            // Academic Information
            student.setClassId(classId);
            
            if (rawDTO.getEnrollmentDate() != null) {
                student.setEnrollmentDate(LocalDate.parse(rawDTO.getEnrollmentDate(), DATE_FORMATTER));
            }
            
            // Performance (String → BigDecimal, Integer)
            if (rawDTO.getGpa() != null) {
                student.setGpa(new BigDecimal(rawDTO.getGpa()));
            }
            
            if (rawDTO.getTotalCredits() != null) {
                student.setTotalCredits(Integer.parseInt(rawDTO.getTotalCredits()));
            }
            
            // Status (String → Enum)
            if (rawDTO.getStatus() != null) {
                student.setStatus(StudentStatus.fromString(rawDTO.getStatus()));
            }
            
            logger.debug("Transformed student: {}", student.getStudentId());
            return student;
            
        } catch (Exception e) {
            logger.error("Error transforming student {}: {}", rawDTO.getStudentId(), e.getMessage());
            throw new RuntimeException("Transform failed for student " + rawDTO.getStudentId(), e);
        }
    }
}