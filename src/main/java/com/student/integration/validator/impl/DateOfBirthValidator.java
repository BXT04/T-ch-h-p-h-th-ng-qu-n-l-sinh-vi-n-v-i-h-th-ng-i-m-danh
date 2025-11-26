package com.student.integration.validator.impl;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import com.student.integration.validator.AbstractValidator;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator cho Date of Birth
 * - Format: yyyy-MM-dd
 * - Age: 17-30 tuổi
 */
public class DateOfBirthValidator extends AbstractValidator {
    
    private static final String RULE_NAME = "DateOfBirthRule";
    private static final int MIN_AGE = 17;
    private static final int MAX_AGE = 30;
    
    @Override
    protected void doValidate(StudentRawDTO student, ValidationResult result) {
        String dobString = student.getDateOfBirth();
        
        // Check null/empty
        if (isNullOrEmpty(dobString)) {
            addError(result, ErrorType.MISSING_FIELD, "date_of_birth",
                "Date of birth is required",
                dobString, RULE_NAME, Severity.CRITICAL);
            return;
        }
        
        // Try parse date
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            addError(result, ErrorType.INVALID_FORMAT, "date_of_birth",
                "Date of birth must be in format yyyy-MM-dd (e.g., 2003-05-15)",
                dobString, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Check if date is in the future
        if (dob.isAfter(LocalDate.now())) {
            addError(result, ErrorType.BUSINESS_RULE_VIOLATION, "date_of_birth",
                "Date of birth cannot be in the future",
                dobString, RULE_NAME, Severity.HIGH);
            return;
        }
        
        // Calculate age
        int age = Period.between(dob, LocalDate.now()).getYears();
        
        // Check age range
        if (age < MIN_AGE) {
            addError(result, ErrorType.OUT_OF_RANGE, "date_of_birth",
                String.format("Student is too young (age: %d, minimum: %d)", age, MIN_AGE),
                dobString, RULE_NAME, Severity.HIGH);
        } else if (age > MAX_AGE) {
            addError(result, ErrorType.OUT_OF_RANGE, "date_of_birth",
                String.format("Student is too old (age: %d, maximum: %d)", age, MAX_AGE),
                dobString, RULE_NAME, Severity.MEDIUM);
        }
    }
}