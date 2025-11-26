package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

import java.math.BigDecimal;

/**
 * Validator cho GPA
 * Range: 0.0 - 4.0
 */
public class GPAValidator extends AbstractValidator {
    
    private static final String RULE_NAME = "GPARangeRule";
    private static final BigDecimal MIN_GPA = new BigDecimal("0.0");
    private static final BigDecimal MAX_GPA = new BigDecimal("4.0");
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String gpaString = student.getGpa();
        
        // Check null/empty
        if (isNullOrEmpty(gpaString)) {
            addError(result, ErrorType.MISSING_FIELD, "gpa",
                "GPA is required",
                gpaString, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Try parse as number
        BigDecimal gpa;
        try {
            gpa = new BigDecimal(gpaString.trim());
        } catch (NumberFormatException e) {
            addError(result, ErrorType.INVALID_FORMAT, "gpa",
                "GPA must be a number (e.g., 3.45)",
                gpaString, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Check range
        if (gpa.compareTo(MIN_GPA) < 0) {
            addError(result, ErrorType.OUT_OF_RANGE, "gpa",
                String.format("GPA cannot be less than %.1f", MIN_GPA),
                gpaString, RULE_NAME, Severity.HIGH);
        } else if (gpa.compareTo(MAX_GPA) > 0) {
            addError(result, ErrorType.OUT_OF_RANGE, "gpa",
                String.format("GPA cannot be greater than %.1f", MAX_GPA),
                gpaString, RULE_NAME, Severity.HIGH);
        }
    }
}