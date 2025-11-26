package com.student.integration.consumer;

import com.student.integration.config.DatabaseConfig;
import com.student.integration.config.MessagePublisher;
import com.student.integration.loader.StudentLoader;
import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.entity.Student;
import com.student.integration.transformer.StudentTransformer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer để transform và load students vào Clean DB
 * 
 * Flow:
 * 1. Receive from student.validated
 * 2. Transform (String → proper types)
 * 3. Load to MySQL Clean DB
 * 4. Publish to student.transformed (tracking)
 */
public class TransformerLoaderConsumer extends BaseConsumer {
    
    private final StudentTransformer transformer;
    private final StudentLoader loader;
    private final MessagePublisher messagePublisher;
    
    // Statistics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    
    private long startTime;
    
    public TransformerLoaderConsumer() {
        super();
        this.transformer = new StudentTransformer();
        this.loader = new StudentLoader();
        this.messagePublisher = new MessagePublisher();
        
        // Test DB connection
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        if (!dbConfig.testConnections()) {
            throw new RuntimeException("Database connection test failed");
        }
        
        logger.info("✅ TransformerLoaderConsumer initialized");
    }
    
    @Override
    protected void processMessage(byte[] messageBody) throws Exception {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        
        // 1. Deserialize ValidationResult
        String jsonMessage = new String(messageBody, "UTF-8");
        ValidationResult validationResult = objectMapper.readValue(jsonMessage, ValidationResult.class);
        
        StudentRawDTO rawStudent = validationResult.getRawData();
        
        try {
            // 2. Get class_id
            Integer classId = loader.getClassId(rawStudent.getClassCode());
            if (classId == null) {
                logger.warn("Class code not found: {} for student {}", 
                    rawStudent.getClassCode(), rawStudent.getStudentId());
                // Use default class
                classId = 1;
            }
            
            // 3. Transform
            Student student = transformer.transform(rawStudent, classId);
            
            // 4. Load to DB
            loader.insertStudent(student);
            
            // 5. Publish to transformed queue (for tracking)
            messagePublisher.publishToTransformed(student);
            
            successCount.incrementAndGet();
            logger.debug("✓ Loaded student: {}", student.getStudentId());
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("✗ Failed to process student {}: {}", 
                rawStudent.getStudentId(), e.getMessage());
            throw e;
        }
        
        // Update statistics
        int processed = totalProcessed.incrementAndGet();
        
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
        
        logger.info("📊 Processed: {} | Success: {} | Errors: {} | Rate: {:.1f} msg/s", 
            processed, successCount.get(), errorCount.get(), rate);
    }
    
    /**
     * Print final statistics
     */
    public void printStatistics() throws Exception {
        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;
        double rate = totalProcessed.get() / seconds;
        
        int dbCount = loader.countStudents();
        
        logger.info("\n╔════════════════════════════════════════════════╗");
        logger.info("║     TRANSFORM & LOAD STATISTICS                ║");
        logger.info("╠════════════════════════════════════════════════╣");
        logger.info("║ Total Processed:    {:>27} ║", totalProcessed.get());
        logger.info("║ Successfully Loaded:{:>27} ║", successCount.get());
        logger.info("║ Errors:             {:>27} ║", errorCount.get());
        logger.info("║ Processing Time:    {:>24.2f}s ║", seconds);
        logger.info("║ Throughput:         {:>21.1f} msg/s ║", rate);
        logger.info("║                                                ║");
        logger.info("║ Students in Clean DB: {:>24} ║", dbCount);
        logger.info("╚════════════════════════════════════════════════╝\n");
    }
    
    @Override
    public void stop() throws IOException, TimeoutException {
        try {
            printStatistics();
        } catch (Exception e) {
            logger.error("Error printing statistics", e);
        }
        super.stop();
    }
}