package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

/**
 * Validator cho Class Code
 * Format: 2 chữ + 2 số + 1 chữ + 2 số (VD: SE01K01)
 */
public class ClassCodeValidator extends AbstractValidator {
    
    private static final String CLASS_CODE_REGEX = "^[A-Z]{2}\\d{2}[A-Z]\\d{2}$";
    private static final String RULE_NAME = "ClassCodeFormatRule";
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String classCode = student.getClassCode();
        
        // Check null/empty
        if (isNullOrEmpty(classCode)) {
            addError(result, ErrorType.MISSING_FIELD, "class_code",
                "Class code is required",
                classCode, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Check format
        if (!matchesPattern(classCode, CLASS_CODE_REGEX)) {
            addError(result, ErrorType.INVALID_FORMAT, "class_code",
                "Class code must match format: 2 letters + 2 digits + 1 letter + 2 digits (e.g., SE01K01)",
                classCode, RULE_NAME, Severity.HIGH);
        }
    }
}