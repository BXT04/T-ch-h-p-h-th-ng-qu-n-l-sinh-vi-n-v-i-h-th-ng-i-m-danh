package com.student.integration.validator;

import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;

/**
 * Interface cho tất cả validators
 * Implement Chain of Responsibility pattern
 */
public interface Validator {
    
    /**
     * Validate student data và trả về result
     * 
     * @param student Dữ liệu cần validate
     * @param result ValidationResult để add errors vào
     */
    void validate(StudentRawDTO student, ValidationResult result);
    
    /**
     * Set validator tiếp theo trong chain
     */
    void setNext(Validator next);
    
    /**
     * Get validator tiếp theo
     */
    Validator getNext();
}