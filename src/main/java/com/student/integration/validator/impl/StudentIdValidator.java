package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

/**
 * Validator cho Student ID
 * Format: SV + 8 digits (VD: SV20210001)
 */
public class StudentIdValidator extends AbstractValidator {
    
    private static final String STUDENT_ID_REGEX = "^SV\\d{8}$";
    private static final String RULE_NAME = "StudentIdFormatRule";
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String studentId = student.getStudentId();
        
        // Check null/empty
        if (isNullOrEmpty(studentId)) {
            addError(result, ErrorType.MISSING_FIELD, "student_id",
                "Student ID is required",
                studentId, RULE_NAME, Severity.CRITICAL);
            return;
        }
        
        // Check format
        if (!matchesPattern(studentId, STUDENT_ID_REGEX)) {
            addError(result, ErrorType.INVALID_FORMAT, "student_id",
                "Student ID must match format SV + 8 digits (e.g., SV20210001)",
                studentId, RULE_NAME, Severity.HIGH);
        }
    }
}