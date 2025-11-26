package com.student.integration.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import com.student.integration.config.MessagePublisher;
import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import com.student.integration.model.dto.StudentRawDTO;
import com.student.integration.model.dto.ValidationResult;
import com.student.integration.validator.ValidationChainBuilder;
import com.student.integration.validator.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test validator với số lượng messages giới hạn
 * Dùng để test nhanh với 1000 messages đầu tiên
 */
public class ValidatorConsumerLimitedTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidatorConsumerLimitedTest.class);
    private static final int LIMIT = 1000; // Process 1000 messages để test
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║   VALIDATOR CONSUMER TEST (Limited 1000)     ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        try {
            // Setup
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            RabbitMQConfig config = RabbitMQConfig.getInstance();
            Connection connection = config.getConnection();
            Channel channel = connection.createChannel();
            
            // Create validator và publisher
            Validator validationChain = ValidationChainBuilder.buildStudentValidationChain();
            MessagePublisher publisher = new MessagePublisher();
            
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            // Statistics
            int processed = 0;
            int valid = 0;
            int invalid = 0;
            
            long startTime = System.currentTimeMillis();
            
            logger.info("Processing {} messages from 'student.raw'...\n", LIMIT);
            
            // Process limited messages
            while (processed < LIMIT) {
                // Get message from queue
                GetResponse response = channel.basicGet(queueManager.getQueueRaw(), false);
                
                if (response == null) {
                    logger.info("No more messages in queue");
                    break;
                }
                
                try {
                    // Deserialize
                    String jsonMessage = new String(response.getBody(), "UTF-8");
                    StudentRawDTO student = objectMapper.readValue(jsonMessage, StudentRawDTO.class);
                    
                    // Validate
                    ValidationResult result = new ValidationResult(student);
                    validationChain.validate(student, result);
                    
                    // Route
                    if (result.isValid()) {
                        publisher.publishToValidated(result);
                        valid++;
                    } else {
                        publisher.publishToError(result);
                        invalid++;
                    }
                    
                    // ACK
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    processed++;
                    
                    // Progress
                    if (processed % 100 == 0) {
                        logger.info("Processed: {} | Valid: {} | Invalid: {}", 
                            processed, valid, invalid);
                    }
                    
                } catch (Exception e) {
                    logger.error("Error processing message", e);
                    channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                }
            }
            
            // Statistics
            long elapsed = System.currentTimeMillis() - startTime;
            double seconds = elapsed / 1000.0;
            double rate = processed / seconds;
            
            logger.info("\n╔════════════════════════════════════════════════╗");
            logger.info("║        VALIDATION RESULTS (Limited Test)       ║");
            logger.info("╠════════════════════════════════════════════════╣");
            logger.info("║ Total Processed:    {:>27} ║", processed);
            logger.info("║ Valid Records:      {:>27} ║", valid);
            logger.info("║ Invalid Records:    {:>27} ║", invalid);
            logger.info("║ Validation Rate:    {:>27.2f} ║", 
                (valid * 100.0 / processed));
            logger.info("║ Processing Time:    {:>24.2f}s ║", seconds);
            logger.info("║ Throughput:         {:>21.1f} msg/s ║", rate);
            logger.info("╚════════════════════════════════════════════════╝\n");
            
            logger.info("🔍 Check queues:");
            logger.info("   - student.validated: should have ~{} messages", valid);
            logger.info("   - student.error: should have ~{} messages", invalid);
            
            // Cleanup
            channel.close();
            config.closeConnection();
            
        } catch (Exception e) {
            logger.error("❌ Test failed", e);
        }
    }
}