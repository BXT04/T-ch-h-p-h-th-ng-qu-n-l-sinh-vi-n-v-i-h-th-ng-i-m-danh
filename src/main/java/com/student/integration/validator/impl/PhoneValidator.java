package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

/**
 * Validator cho Phone number
 * Format: 0xxxxxxxxx (10 digits) hoặc +84xxxxxxxxx
 */
public class PhoneValidator extends AbstractValidator {
    
    private static final String PHONE_REGEX = "^(0|\\+84)[0-9]{9}$";
    private static final String RULE_NAME = "PhoneFormatRule";
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String phone = student.getPhone();
        
        // Check null/empty
        if (isNullOrEmpty(phone)) {
            addError(result, ErrorType.MISSING_FIELD, "phone",
                "Phone number is required",
                phone, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Remove spaces and dashes for validation
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        
        // Check format
        if (!matchesPattern(cleanPhone, PHONE_REGEX)) {
            addError(result, ErrorType.INVALID_FORMAT, "phone",
                "Phone number must be 10 digits starting with 0 or +84 (e.g., 0901234567)",
                phone, RULE_NAME, Severity.HIGH);
        }
    }
}