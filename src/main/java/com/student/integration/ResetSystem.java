package com.student.integration;

import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetSystem {
    
    private static final Logger logger = LoggerFactory.getLogger(ResetSystem.class);
    
    public static void main(String[] args) {
        logger.info("🧹 Resetting system...\n");
        
        try {
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            logger.info("⚠️  Purging all queues...");
            queueManager.purgeAllQueues();
            
            logger.info("✅ System reset completed\n");
            
            RabbitMQConfig.getInstance().closeConnection();
            
        } catch (Exception e) {
            logger.error("Reset failed", e);
        }
    }
}