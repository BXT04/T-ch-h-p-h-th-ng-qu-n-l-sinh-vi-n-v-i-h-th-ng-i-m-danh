package com.student.integration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class để verify RabbitMQ configuration
 */
public class RabbitMQTest {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║   RABBITMQ CONNECTION & QUEUE TEST           ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        try {
            // 1. Test connection
            logger.info("1️⃣  Testing RabbitMQ connection...");
            RabbitMQConfig config = RabbitMQConfig.getInstance();
            boolean connected = config.testConnection();
            
            if (!connected) {
                logger.error("❌ Connection failed. Please check:");
                logger.error("   - Docker containers are running (docker ps)");
                logger.error("   - RabbitMQ is accessible at localhost:5672");
                logger.error("   - Credentials: admin/admin123");
                return;
            }
            
            // 2. Setup queues
            logger.info("\n2️⃣  Setting up queues and exchanges...");
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // 3. Success
            logger.info("\n✅ All tests passed!");
            logger.info("📊 Visit RabbitMQ Management UI: http://localhost:15672");
            logger.info("   Username: admin");
            logger.info("   Password: admin123");
            
            // Cleanup
            config.closeConnection();
            
        } catch (Exception e) {
            logger.error("❌ Test failed", e);
        }
    }
}