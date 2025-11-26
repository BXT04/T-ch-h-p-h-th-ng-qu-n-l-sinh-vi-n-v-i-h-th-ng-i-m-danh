package com.student.integration.consumer;

import com.student.integration.config.DatabaseConfig;
import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Transformer & Loader Consumer
 */
public class TransformerLoaderConsumerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformerLoaderConsumerTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║   TRANSFORMER & LOADER CONSUMER TEST         ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        TransformerLoaderConsumer consumer = null;
        
        try {
            // 1. Test DB connections
            logger.info("1️⃣  Testing database connections...");
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (!dbConfig.testConnections()) {
                logger.error("❌ Database connection failed");
                return;
            }
            
            // 2. Setup queues
            logger.info("\n2️⃣  Setting up queues...");
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // 3. Create consumer
            logger.info("\n3️⃣  Creating Transformer & Loader Consumer...");
            consumer = new TransformerLoaderConsumer();
            
            // 4. Start consuming
            logger.info("\n4️⃣  Starting to consume from 'student.validated'...");
            String queueName = queueManager.getQueueValidated();
            
            // Add shutdown hook
            TransformerLoaderConsumer finalConsumer = consumer;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("\n⚠️  Shutdown signal received...");
                try {
                    finalConsumer.stop();
                    DatabaseConfig.getInstance().close();
                    RabbitMQConfig.getInstance().closeConnection();
                } catch (Exception e) {
                    logger.error("Error during shutdown", e);
                }
            }));
            
            consumer.startConsuming(queueName, 10);
            
            // Wait
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
                DatabaseConfig.getInstance().close();
                RabbitMQConfig.getInstance().closeConnection();
            } catch (Exception e) {
                logger.error("Error closing connections", e);
            }
        }
    }
}