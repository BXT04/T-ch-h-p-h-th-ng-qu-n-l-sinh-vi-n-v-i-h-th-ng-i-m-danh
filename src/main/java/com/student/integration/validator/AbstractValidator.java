package com.student.integration.validator;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationError;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class cho validators
 * Implement Chain of Responsibility logic
 */
public abstract class AbstractValidator implements Validator {
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractValidator.class);
    
    private Validator next;
    
    @Override
    public void validate(StudentRawDTO student, ValidationResult result) {
        // Thực hiện validation của validator này
        doValidate(student, result);
        
        // Tiếp tục chain (dù pass hay fail)
        if (next != null) {
            next.validate(student, result);
        }
    }
    
    /**
     * Template method - subclass implement logic cụ thể
     */
    protected abstract void doValidate(StudentRawDTO student, ValidationResult result);
    
    @Override
    public void setNext(Validator next) {
        this.next = next;
    }
    
    @Override
    public Validator getNext() {
        return next;
    }
    
    /**
     * Helper method để add error vào result
     */
    protected void addError(ValidationResult result, ErrorType errorType, 
                           String field, String message, String invalidValue, 
                           String rule, Severity severity) {
        ValidationError error = new ValidationError(
            errorType, field, message, invalidValue, rule, severity
        );
        result.addError(error);
        
        logger.debug("Validation error: {} - {} (Value: {})", field, message, invalidValue);
    }
    
    /**
     * Helper: Check if string is null or empty
     */
    protected boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Helper: Check if string matches regex
     */
    protected boolean matchesPattern(String value, String regex) {
        if (value == null) return false;
        return value.matches(regex);
    }
}