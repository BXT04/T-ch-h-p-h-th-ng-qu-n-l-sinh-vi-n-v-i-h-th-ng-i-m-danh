package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

/**
 * Validator cho Email
 */
public class EmailValidator extends AbstractValidator {
    
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String RULE_NAME = "EmailFormatRule";
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String email = student.getEmail();
        
        // Check null/empty
        if (isNullOrEmpty(email)) {
            addError(result, ErrorType.MISSING_FIELD, "email",
                "Email is required",
                email, RULE_NAME, Severity.CRITICAL);
            return;
        }
        
        // Check format
        if (!matchesPattern(email, EMAIL_REGEX)) {
            addError(result, ErrorType.INVALID_FORMAT, "email",
                "Email format is invalid (e.g., user@domain.com)",
                email, RULE_NAME, Severity.HIGH);
        }
        
        // Additional check: length
        if (email.length() > 255) {
            addError(result, ErrorType.OUT_OF_RANGE, "email",
                "Email is too long (max 255 characters)",
                email, RULE_NAME, Severity.MEDIUM);
        }
    }
}