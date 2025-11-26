package com.student.integration.producer;

import com.student.integration.config.MessagePublisher;
import com.student.integration.model.dto.StudentRawDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer để gửi student records vào RabbitMQ
 */
public class StudentProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentProducer.class);
    
    private final CSVReader csvReader;
    private final MessagePublisher messagePublisher;
    
    public StudentProducer() {
        this.csvReader = new CSVReader();
        this.messagePublisher = new MessagePublisher();
    }
    
    /**
     * Đọc CSV file và publish tất cả records vào queue (Batch mode)
     */
    public void publishFromCSVBatch(Path csvFile) throws IOException, TimeoutException {
        logger.info("📂 Reading CSV file (batch mode): {}", csvFile.getFileName());
        
        // Read all records
        List<StudentRawDTO> students = csvReader.readCSV(csvFile);
        
        logger.info("📤 Publishing {} records to RabbitMQ...", students.size());
        
        // Publish to queue
        int successCount = 0;
        int errorCount = 0;
        
        for (StudentRawDTO student : students) {
            try {
                messagePublisher.publishToRaw(student);
                successCount++;
                
                if (successCount % 1000 == 0) {
                    logger.info("Published {}/{} records", successCount, students.size());
                }
                
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to publish student {}: {}", 
                    student.getStudentId(), e.getMessage());
            }
        }
        
        logger.info("✅ Batch publish completed: {} success, {} errors", 
            successCount, errorCount);
    }
    
    /**
     * Đọc CSV file và publish realtime (Streaming mode)
     * Mỗi record được publish NGAY KHI ĐỌC
     */
    public void publishFromCSVStreaming(Path csvFile) throws IOException {
        logger.info("📂 Reading CSV file (streaming mode): {}", csvFile.getFileName());
        logger.info("🚀 Realtime processing: records will be published immediately");
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Read with callback
        csvReader.readCSVStreaming(csvFile, new CSVReader.RecordCallback() {
            
            @Override
            public void onRecord(StudentRawDTO student, int recordNumber) throws Exception {
                // Publish ngay khi đọc được record
                messagePublisher.publishToRaw(student);
                successCount.incrementAndGet();
            }
            
            @Override
            public void onError(int rowNumber, Exception error) {
                errorCount.incrementAndGet();
                logger.error("Row {}: {}", rowNumber, error.getMessage());
            }
            
            @Override
            public void onComplete(int totalProcessed, int totalErrors) {
                logger.info("╔════════════════════════════════════════╗");
                logger.info("║     STREAMING PUBLISH COMPLETED        ║");
                logger.info("╠════════════════════════════════════════╣");
                logger.info("║ Total Records:    {:>20} ║", totalProcessed);
                logger.info("║ Successfully Published: {:>14} ║", successCount.get());
                logger.info("║ Failed:           {:>20} ║", errorCount.get());
                logger.info("╚════════════════════════════════════════╝");
            }
        });
    }
    
    /**
     * Publish single student record (For adding new student)
     */
    public void publishSingleStudent(StudentRawDTO student) throws IOException, TimeoutException {
        logger.info("📤 Publishing single student: {}", student.getStudentId());
        messagePublisher.publishToRaw(student);
        logger.info("✅ Student published successfully");
    }
    
    /**
     * Publish list of students
     */
    public void publishStudents(List<StudentRawDTO> students) throws IOException, TimeoutException {
        logger.info("📤 Publishing {} students to queue...", students.size());
        
        int count = 0;
        for (StudentRawDTO student : students) {
            try {
                messagePublisher.publishToRaw(student);
                count++;
                
                if (count % 100 == 0) {
                    logger.info("Published {}/{}", count, students.size());
                }
            } catch (Exception e) {
                logger.error("Failed to publish student {}", student.getStudentId(), e);
            }
        }
        
        logger.info("✅ Published {} students", count);
    }
}