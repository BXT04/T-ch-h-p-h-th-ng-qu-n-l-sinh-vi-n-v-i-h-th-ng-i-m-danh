package com.student.integration.producer;

import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Producer - Đọc CSV và publish vào RabbitMQ
 */
public class ProducerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║        CSV PRODUCER TEST                     ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        try {
            // 1. Setup RabbitMQ
            logger.info("1️⃣  Setting up RabbitMQ...");
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // 2. Find CSV file
            logger.info("\n2️⃣  Looking for CSV file...");
            Path csvFile = Paths.get("./data/generated/students_messy_20k.csv");
            
            if (!Files.exists(csvFile)) {
                logger.error("❌ CSV file not found: {}", csvFile);
                logger.info("Please generate CSV first using MessyDataGenerator");
                return;
            }
            
            logger.info("✅ Found CSV file: {} ({} bytes)", 
                csvFile.getFileName(), Files.size(csvFile));
            
            // 3. Publish using STREAMING mode (Realtime)
            logger.info("\n3️⃣  Publishing to RabbitMQ (Streaming Mode)...");
            StudentProducer producer = new StudentProducer();
            
            long startTime = System.currentTimeMillis();
            producer.publishFromCSVStreaming(csvFile);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("\n⏱️  Total time: {} ms ({} seconds)", duration, duration / 1000.0);
            
            // 4. Check queue
            logger.info("\n4️⃣  Checking queue status...");
            logger.info("📊 Visit RabbitMQ Management: http://localhost:15672");
            logger.info("   Go to 'Queues' tab and check 'student.raw' queue");
            
            // Close connection
            RabbitMQConfig.getInstance().closeConnection();
            logger.info("\n✅ Test completed successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Test failed", e);
        }
    }
}