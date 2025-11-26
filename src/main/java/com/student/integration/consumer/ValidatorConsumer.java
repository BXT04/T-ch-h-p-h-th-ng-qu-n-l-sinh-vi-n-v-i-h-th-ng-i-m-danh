package com.student.integration.consumer;

import com.student.integration.config.MessagePublisher;
import com.student.integration.config.QueueManager;
import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.validator.ValidationChainBuilder;
import com.student.integration.validator.Validator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer để validate student messages từ queue "student.raw"
 * 
 * Flow:
 * 1. Receive message từ student.raw
 * 2. Deserialize JSON → StudentRawDTO
 * 3. Apply validation chain
 * 4. Route:
 *    - Valid → student.validated
 *    - Invalid → student.error
 */
public class ValidatorConsumer extends BaseConsumer {
    
    private final Validator validationChain;
    private final MessagePublisher messagePublisher;
    private final QueueManager queueManager;
    
    // Statistics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger validCount = new AtomicInteger(0);
    private final AtomicInteger invalidCount = new AtomicInteger(0);
    
    private long startTime;
    
    public ValidatorConsumer() {
        super();
        this.validationChain = ValidationChainBuilder.buildStudentValidationChain();
        this.messagePublisher = new MessagePublisher();
        this.queueManager = new QueueManager();
        
        logger.info("✅ ValidatorConsumer initialized with validation chain");
    }
    
    @Override
    protected void processMessage(byte[] messageBody) throws Exception {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        
        // 1. Deserialize JSON
        String jsonMessage = new String(messageBody, "UTF-8");
        StudentRawDTO student = objectMapper.readValue(jsonMessage, StudentRawDTO.class);
        
        logger.debug("Processing student: {} (Row {})", 
            student.getStudentId(), student.getRowNum());
        
        // 2. Validate
        ValidationResult result = new ValidationResult(student);
        validationChain.validate(student, result);
        
        // 3. Route based on validation result
        if (result.isValid()) {
            // Valid → publish to validated queue
            messagePublisher.publishToValidated(result);
            validCount.incrementAndGet();
            
            logger.debug("✓ Valid: {}", student.getStudentId());
            
        } else {
            // Invalid → publish to error queue
            messagePublisher.publishToError(result);
            invalidCount.incrementAndGet();
            
            logger.debug("✗ Invalid: {} - {} error(s)", 
                student.getStudentId(), result.getErrorCount());
        }
        
        // 4. Update statistics
        int processed = totalProcessed.incrementAndGet();
        
        // Progress log mỗi 100 records
        if (processed % 100 == 0) {
            logProgress(processed);
        }
    }
    
    /**
     * Log progress
     */
    private void logProgress(int processed) {
        long elapsed = System.currentTimeMillis() - startTime;
        double rate = processed / (elapsed / 1000.0);
        
        logger.info("📊 Processed: {} | Valid: {} | Invalid: {} | Rate: {:.1f} msg/s", 
            processed, validCount.get(), invalidCount.get(), rate);
    }
    
    /**
     * Print final statistics
     */
    public void printStatistics() {
        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;
        double rate = totalProcessed.get() / seconds;
        
        logger.info("\n╔════════════════════════════════════════════════╗");
        logger.info("║        VALIDATION STATISTICS                   ║");
        logger.info("╠════════════════════════════════════════════════╣");
        logger.info("║ Total Processed:    {:>27} ║", totalProcessed.get());
        logger.info("║ Valid Records:      {:>27} ║", validCount.get());
        logger.info("║ Invalid Records:    {:>27} ║", invalidCount.get());
        logger.info("║ Validation Rate:    {:>27.2f} ║", 
            (validCount.get() * 100.0 / totalProcessed.get()));
        logger.info("║ Processing Time:    {:>24.2f}s ║", seconds);
        logger.info("║ Throughput:         {:>21.1f} msg/s ║", rate);
        logger.info("╚════════════════════════════════════════════════╝\n");
    }
    
    @Override
    public void stop() throws IOException, TimeoutException {
        printStatistics();
        super.stop();
    }
}