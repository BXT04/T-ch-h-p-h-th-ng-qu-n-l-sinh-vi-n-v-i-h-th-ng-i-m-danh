package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

/**
 * Validator kiểm tra các trường bắt buộc
 */
public class RequiredFieldsValidator extends AbstractValidator {
    
    private static final String RULE_NAME = "RequiredFieldsRule";
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        
        // Full name
        if (isNullOrEmpty(student.getFullName())) {
            addError(result, ErrorType.MISSING_FIELD, "full_name",
                "Full name is required",
                student.getFullName(), RULE_NAME, Severity.CRITICAL);
        }
        
        // Gender
        if (isNullOrEmpty(student.getGender())) {
            addError(result, ErrorType.MISSING_FIELD, "gender",
                "Gender is required",
                student.getGender(), RULE_NAME, Severity.HIGH);
        }
        
        // Major
        if (isNullOrEmpty(student.getMajor())) {
            addError(result, ErrorType.MISSING_FIELD, "major",
                "Major is required",
                student.getMajor(), RULE_NAME, Severity.MEDIUM);
        }
        
        // Faculty
        if (isNullOrEmpty(student.getFaculty())) {
            addError(result, ErrorType.MISSING_FIELD, "faculty",
                "Faculty is required",
                student.getFaculty(), RULE_NAME, Severity.MEDIUM);
        }
        
        // Status
        if (isNullOrEmpty(student.getStatus())) {
            addError(result, ErrorType.MISSING_FIELD, "status",
                "Status is required",
                student.getStatus(), RULE_NAME, Severity.MEDIUM);
        }
    }
}