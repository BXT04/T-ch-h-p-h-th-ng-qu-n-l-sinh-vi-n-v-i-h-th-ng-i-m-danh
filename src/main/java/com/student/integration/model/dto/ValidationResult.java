package com.student.integration.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả validation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("raw_data")
    private StudentRawDTO rawData;
    
    @JsonProperty("is_valid")
    private boolean isValid;
    
    @JsonProperty("errors")
    private List<ValidationError> errors;
    
    @JsonProperty("validation_timestamp")
    private LocalDateTime validationTimestamp;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.isValid = true;
        this.validationTimestamp = LocalDateTime.now();
    }
    
    public ValidationResult(StudentRawDTO rawData) {
        this();
        this.rawData = rawData;
    }
    
    // Getters and Setters
    public StudentRawDTO getRawData() {
        return rawData;
    }
    
    public void setRawData(StudentRawDTO rawData) {
        this.rawData = rawData;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValid(boolean valid) {
        isValid = valid;
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
    
    public LocalDateTime getValidationTimestamp() {
        return validationTimestamp;
    }
    
    public void setValidationTimestamp(LocalDateTime validationTimestamp) {
        this.validationTimestamp = validationTimestamp;
    }
    
    // Helper methods
    public void addError(ValidationError error) {
        this.errors.add(error);
        this.isValid = false;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No errors";
        }
        
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            sb.append(error.getErrorField())
              .append(": ")
              .append(error.getErrorMessage())
              .append("; ");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "isValid=" + isValid +
                ", errorCount=" + errors.size() +
                ", timestamp=" + validationTimestamp +
                '}';
    }
}