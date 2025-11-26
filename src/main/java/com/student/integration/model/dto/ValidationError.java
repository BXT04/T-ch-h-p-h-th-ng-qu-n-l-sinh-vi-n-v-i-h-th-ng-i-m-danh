package com.student.integration.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.student.integration.model.enums.ErrorType;
import com.student.integration.model.enums.Severity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO cho một lỗi validation
 */
public class ValidationError implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("error_type")
    private ErrorType errorType;
    
    @JsonProperty("error_field")
    private String errorField;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("invalid_value")
    private String invalidValue;
    
    @JsonProperty("validation_rule")
    private String validationRule;
    
    @JsonProperty("severity")
    private Severity severity;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    // Constructor
    public ValidationError() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ValidationError(ErrorType errorType, String errorField, String errorMessage) {
        this();
        this.errorType = errorType;
        this.errorField = errorField;
        this.errorMessage = errorMessage;
        this.severity = Severity.MEDIUM; // Default
    }
    
    public ValidationError(ErrorType errorType, String errorField, String errorMessage, 
                          String invalidValue, String validationRule, Severity severity) {
        this();
        this.errorType = errorType;
        this.errorField = errorField;
        this.errorMessage = errorMessage;
        this.invalidValue = invalidValue;
        this.validationRule = validationRule;
        this.severity = severity;
    }
    
    // Getters and Setters
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
    
    public String getErrorField() {
        return errorField;
    }
    
    public void setErrorField(String errorField) {
        this.errorField = errorField;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getInvalidValue() {
        return invalidValue;
    }
    
    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }
    
    public String getValidationRule() {
        return validationRule;
    }
    
    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s (Field: %s, Value: %s)", 
            severity, errorType, errorMessage, errorField, invalidValue);
    }
}