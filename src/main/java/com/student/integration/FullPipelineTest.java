package com.student.integration;

import com.student.integration.config.DatabaseConfig;
import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import com.student.integration.consumer.TransformerLoaderConsumer;
import com.student.integration.consumer.ValidatorConsumer;
import com.student.integration.producer.StudentProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Full ETL Pipeline Test
 * Test toàn bộ flow: CSV → Producer → Validator → Transformer → MySQL
 */
public class FullPipelineTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FullPipelineTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║          FULL ETL PIPELINE TEST                      ║");
        logger.info("║  CSV → RabbitMQ → Validate → Transform → MySQL       ║");
        logger.info("╚══════════════════════════════════════════════════════╝\n");
        
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ValidatorConsumer validatorConsumer = null;
        TransformerLoaderConsumer transformerConsumer = null;
        
        try {
            // 1. Setup
            logger.info("📋 STEP 1: Setting up infrastructure...\n");
            
            // Test DB
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (!dbConfig.testConnections()) {
                throw new RuntimeException("Database connection failed");
            }
            
            // Setup queues
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // 2. Start Consumers (in background threads)
            logger.info("\n📋 STEP 2: Starting consumers...\n");
            
            validatorConsumer = new ValidatorConsumer();
            ValidatorConsumer finalValidatorConsumer = validatorConsumer;
            executorService.submit(() -> {
                try {
                    logger.info("🔹 Validator Consumer started");
                    finalValidatorConsumer.startConsuming(queueManager.getQueueRaw(), 50);
                    finalValidatorConsumer.waitUntilInterrupted();
                } catch (Exception e) {
                    logger.error("Validator Consumer error", e);
                }
            });
            
            // Wait a bit for consumer to start
            Thread.sleep(2000);
            
            transformerConsumer = new TransformerLoaderConsumer();
            TransformerLoaderConsumer finalTransformerConsumer = transformerConsumer;
            executorService.submit(() -> {
                try {
                    logger.info("🔹 Transformer & Loader Consumer started");
                    finalTransformerConsumer.startConsuming(queueManager.getQueueValidated(), 50);
                    finalTransformerConsumer.waitUntilInterrupted();
                } catch (Exception e) {
                    logger.error("Transformer Consumer error", e);
                }
            });
            
            // Wait for consumers to be ready
            Thread.sleep(2000);
            
            // 3. Publish CSV data
            logger.info("\n📋 STEP 3: Publishing CSV data...\n");
            
            Path csvFile = Paths.get("./data/generated/students_messy_20k.csv");
            if (!Files.exists(csvFile)) {
                throw new RuntimeException("CSV file not found: " + csvFile);
            }
            
            StudentProducer producer = new StudentProducer();
            long startTime = System.currentTimeMillis();
            
            logger.info("📤 Starting to publish 20,000 records (streaming mode)...");
            producer.publishFromCSVStreaming(csvFile);
            
            long publishTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Publishing completed in {:.2f} seconds\n", publishTime / 1000.0);
            
            // 4. Wait for processing
            logger.info("📋 STEP 4: Waiting for pipeline to process...\n");
            logger.info("⏳ This will take 2-3 minutes for 20,000 records");
            logger.info("   You can monitor progress in the logs above");
            logger.info("   Press Ctrl+C to stop early\n");
            
            // Wait for processing (or until interrupted)
            Thread.sleep(180000); // 3 minutes
            
            // 5. Stop consumers
            logger.info("\n📋 STEP 5: Stopping consumers...\n");
            
            if (finalValidatorConsumer != null) {
                finalValidatorConsumer.stop();
            }
            
            if (finalTransformerConsumer != null) {
                finalTransformerConsumer.stop();
            }
            
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            
            // 6. Final summary
            logger.info("\n╔════════════════════════════════════════════════╗");
            logger.info("║           PIPELINE TEST COMPLETED             ║");
            logger.info("╚════════════════════════════════════════════════╝");
            logger.info("✅ Check RabbitMQ Management UI: http://localhost:15672");
            logger.info("✅ Check MySQL Clean DB for inserted students");
            
        } catch (Exception e) {
            logger.error("❌ Pipeline test failed", e);
        } finally {
            // Cleanup
            try {
                if (validatorConsumer != null) validatorConsumer.stop();
                if (transformerConsumer != null) transformerConsumer.stop();
                executorService.shutdownNow();
                DatabaseConfig.getInstance().close();
                RabbitMQConfig.getInstance().closeConnection();
            } catch (Exception e) {
                logger.error("Cleanup error", e);
            }
        }
    }
}