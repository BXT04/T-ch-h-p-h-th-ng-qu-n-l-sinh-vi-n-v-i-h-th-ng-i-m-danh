package com.student.integration.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import com.student.integration.config.DatabaseConfig;
import com.student.integration.config.MessagePublisher;
import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import com.student.integration.loader.StudentLoader;
import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.model.entity.Student;
import com.student.integration.transformer.StudentTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug Transformer - Process 100 messages và show errors
 */
public class TransformerDebugTest {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformerDebugTest.class);
    private static final int LIMIT = 100;
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║   TRANSFORMER DEBUG TEST (100 messages)      ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        try {
            // Setup
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (!dbConfig.testConnections()) {
                throw new RuntimeException("Database connection failed");
            }
            
            RabbitMQConfig config = RabbitMQConfig.getInstance();
            Connection connection = config.getConnection();
            Channel channel = connection.createChannel();
            
            // Create components
            StudentTransformer transformer = new StudentTransformer();
            StudentLoader loader = new StudentLoader();
            MessagePublisher publisher = new MessagePublisher();
            
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            objectMapper.registerModule(javaTimeModule);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
            
            // Statistics
            int processed = 0;
            int success = 0;
            int errors = 0;
            
            long startTime = System.currentTimeMillis();
            
            logger.info("Processing {} messages from 'student.validated'...\n", LIMIT);
            
            // Process limited messages
            while (processed < LIMIT) {
                GetResponse response = channel.basicGet(queueManager.getQueueValidated(), false);
                
                if (response == null) {
                    logger.info("No more messages in queue");
                    break;
                }
                
                try {
                    // Deserialize
                    String jsonMessage = new String(response.getBody(), "UTF-8");
                    ValidationResult validationResult = objectMapper.readValue(jsonMessage, ValidationResult.class);
                    
                    StudentRawDTO rawStudent = validationResult.getRawData();
                    
                    logger.debug("Processing: {} - class_code: {}", 
                        rawStudent.getStudentId(), rawStudent.getClassCode());
                    
                    // Get class_id
                    Integer classId = loader.getClassId(rawStudent.getClassCode());
                    if (classId == null) {
                        logger.error("❌ Class code NOT FOUND: {} for student {}", 
                            rawStudent.getClassCode(), rawStudent.getStudentId());
                        errors++;
                        channel.basicNack(response.getEnvelope().getDeliveryTag(), false, false);
                        processed++;
                        continue;
                    }
                    
                    // Transform
                    Student student = transformer.transform(rawStudent, classId);
                    
                    // Load to DB
                    loader.insertStudent(student);
                    
                    // Publish to transformed
                    publisher.publishToTransformed(student);
                    
                    success++;
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    
                    logger.debug("✓ Success: {}", student.getStudentId());
                    
                } catch (Exception e) {
                    errors++;
                    logger.error("❌ Error processing message: {}", e.getMessage());
                    e.printStackTrace();
                    channel.basicNack(response.getEnvelope().getDeliveryTag(), false, false);
                }
                
                processed++;
                
                // Progress
                if (processed % 10 == 0) {
                    logger.info("Progress: {} | Success: {} | Errors: {}", 
                        processed, success, errors);
                }
            }
            
            // Statistics
            long elapsed = System.currentTimeMillis() - startTime;
            double seconds = elapsed / 1000.0;
            
            logger.info("\n╔════════════════════════════════════════════════╗");
            logger.info("║        DEBUG TEST RESULTS                      ║");
            logger.info("╠════════════════════════════════════════════════╣");
            logger.info("║ Processed:    {:>33} ║", processed);
            logger.info("║ Success:      {:>33} ║", success);
            logger.info("║ Errors:       {:>33} ║", errors);
            logger.info("║ Time:         {:>30.2f}s ║", seconds);
            logger.info("╚════════════════════════════════════════════════╝\n");
            
            // Check DB
            int dbCount = loader.countStudents();
            logger.info("📊 Students in DB: {}", dbCount);
            
            // Cleanup
            channel.close();
            config.closeConnection();
            dbConfig.close();
            
        } catch (Exception e) {
            logger.error("❌ Test failed", e);
            e.printStackTrace();
        }
    }
}