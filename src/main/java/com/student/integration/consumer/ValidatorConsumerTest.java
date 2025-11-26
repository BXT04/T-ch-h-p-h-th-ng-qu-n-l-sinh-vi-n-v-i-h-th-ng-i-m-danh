package com.student.integration.consumer;

import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Validator Consumer
 * Consume messages từ student.raw và validate
 */
public class ValidatorConsumerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidatorConsumerTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║      VALIDATOR CONSUMER TEST                 ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        ValidatorConsumer consumer = null;
        
        try {
            // 1. Setup queues
            logger.info("1️⃣  Setting up queues...");
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // 2. Create consumer
            logger.info("\n2️⃣  Creating Validator Consumer...");
            consumer = new ValidatorConsumer();
            
            // 3. Start consuming
            logger.info("\n3️⃣  Starting to consume messages from 'student.raw'...");
            String queueName = queueManager.getQueueRaw();
            
            // Add shutdown hook
            ValidatorConsumer finalConsumer = consumer;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("\n⚠️  Shutdown signal received...");
                try {
                    finalConsumer.stop();
                    RabbitMQConfig.getInstance().closeConnection();
                } catch (IOException | TimeoutException e) {
                    logger.error("Error during shutdown", e);
                }
            }));
            
            consumer.startConsuming(queueName, 10); // Prefetch 10 messages
            
            // Wait until interrupted
            consumer.waitUntilInterrupted();
            
        } catch (Exception e) {
            logger.error("❌ Consumer failed", e);
        } finally {
            // Cleanup
            if (consumer != null) {
                try {
                    consumer.stop();
                } catch (Exception e) {
                    logger.error("Error stopping consumer", e);
                }
            }
            
            try {
                RabbitMQConfig.getInstance().closeConnection();
            } catch (Exception e) {
                logger.error("Error closing connection", e);
            }
        }
    }
}