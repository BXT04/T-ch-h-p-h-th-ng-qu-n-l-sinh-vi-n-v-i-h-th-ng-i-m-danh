package com.student.integration.validator;

import com.student.integration.validator.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class để xây dựng validation chain
 */
public class ValidationChainBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationChainBuilder.class);
    
    /**
     * Build default validation chain cho Student
     */
    public static Validator buildStudentValidationChain() {
        
        // Create validators
        Validator requiredFields = new RequiredFieldsValidator();
        Validator studentId = new StudentIdValidator();
        Validator email = new EmailValidator();
        Validator phone = new PhoneValidator();
        Validator dob = new DateOfBirthValidator();
        Validator gpa = new GPAValidator();
        Validator classCode = new ClassCodeValidator();
        
        // Build chain
        requiredFields.setNext(studentId);
        studentId.setNext(email);
        email.setNext(phone);
        phone.setNext(dob);
        dob.setNext(gpa);
        gpa.setNext(classCode);
        
        logger.info("✅ Validation chain built with 7 validators");
        
        return requiredFields; // Return head of chain
    }
    
    /**
     * Build custom chain
     */
    public static Validator buildCustomChain(Validator... validators) {
        if (validators == null || validators.length == 0) {
            throw new IllegalArgumentException("At least one validator required");
        }
        
        for (int i = 0; i < validators.length - 1; i++) {
            validators[i].setNext(validators[i + 1]);
        }
        
        logger.info("✅ Custom validation chain built with {} validators", validators.length);
        
        return validators[0]; // Return head of chain
    }
}