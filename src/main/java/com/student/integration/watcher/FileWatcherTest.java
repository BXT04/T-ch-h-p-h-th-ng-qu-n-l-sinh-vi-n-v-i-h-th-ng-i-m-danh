package com.student.integration.watcher;

import com.student.integration.config.DatabaseConfig;
import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import com.student.integration.consumer.TransformerLoaderConsumer;
import com.student.integration.consumer.ValidatorConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test REALTIME File Watcher với consumers
 * 
 * CORE FEATURE: "Khi thêm bất kì file CSV sinh viên nào vào để thực hiện check rule"
 * 
 * HOW TO TEST:
 * 1. Run this test
 * 2. Copy CSV file to ./data/input/
 * 3. Watch automatic processing
 * 4. Check students in database
 */
public class FileWatcherTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║      REALTIME FILE WATCHER + AUTO PROCESSING         ║");
        logger.info("║                                                      ║");
        logger.info("║  DROP CSV → Auto Validate → Auto Transform → DB     ║");
        logger.info("╚══════════════════════════════════════════════════════╝\n");
        
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        ValidatorConsumer validatorConsumer = null;
        TransformerLoaderConsumer transformerConsumer = null;
        CSVFileWatcher fileWatcher = null;
        
        try {
            // 1. Setup
            logger.info("📋 STEP 1: Setting up infrastructure...\n");
            
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (!dbConfig.testConnections()) {
                throw new RuntimeException("Database connection failed");
            }
            
            logger.info("\n📋 STEP 2: Starting background consumers...\n");
            
            // 2. Start Validator Consumer (background)
            validatorConsumer = new ValidatorConsumer();
            ValidatorConsumer finalValidatorConsumer = validatorConsumer;
            executorService.submit(() -> {
                try {
                    logger.info("🔹 Validator Consumer: RUNNING");
                    finalValidatorConsumer.startConsuming(queueManager.getQueueRaw(), 50);
                    finalValidatorConsumer.waitUntilInterrupted();
                } catch (Exception e) {
                    logger.error("Validator Consumer error", e);
                }
            });
            
            Thread.sleep(2000);
            
            // 3. Start Transformer Consumer (background)
            transformerConsumer = new TransformerLoaderConsumer();
            TransformerLoaderConsumer finalTransformerConsumer = transformerConsumer;
            executorService.submit(() -> {
                try {
                    logger.info("🔹 Transformer Consumer: RUNNING");
                    finalTransformerConsumer.startConsuming(queueManager.getQueueValidated(), 50);
                    finalTransformerConsumer.waitUntilInterrupted();
                } catch (Exception e) {
                    logger.error("Transformer Consumer error", e);
                }
            });
            
            Thread.sleep(2000);
            
            logger.info("\n📋 STEP 3: Starting File Watcher...\n");
            
            // 4. Start File Watcher
            fileWatcher = new CSVFileWatcher();
            CSVFileWatcher finalFileWatcher = fileWatcher;
            
            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("\n╔══════════════════════════════════════════════════════╗");
                logger.info("║              SHUTTING DOWN SYSTEM                    ║");
                logger.info("╚══════════════════════════════════════════════════════╝");
                try {
                    finalFileWatcher.stop();
                    if (finalValidatorConsumer != null) finalValidatorConsumer.stop();
                    if (finalTransformerConsumer != null) finalTransformerConsumer.stop();
                    executorService.shutdownNow();
                    dbConfig.close();
                    RabbitMQConfig.getInstance().closeConnection();
                    logger.info("✅ System shutdown completed");
                } catch (Exception e) {
                    logger.error("Shutdown error", e);
                }
            }));
            
            logger.info("╔══════════════════════════════════════════════════════╗");
            logger.info("║          SYSTEM READY FOR REALTIME PROCESSING        ║");
            logger.info("╠══════════════════════════════════════════════════════╣");
            logger.info("║                                                      ║");
            logger.info("║  ✅ Validator Consumer:   RUNNING                    ║");
            logger.info("║  ✅ Transformer Consumer: RUNNING                    ║");
            logger.info("║  ✅ File Watcher:         ACTIVE                     ║");
            logger.info("║                                                      ║");
            logger.info("║  📂 Drop CSV files to: ./data/input/                 ║");
            logger.info("║  ⚡ Files will be processed automatically             ║");
            logger.info("║  💾 Check database for results                       ║");
            logger.info("║                                                      ║");
            logger.info("║  ⏹️  Press Ctrl+C to stop                            ║");
            logger.info("╚══════════════════════════════════════════════════════╝\n");
            
            // Start watching
            fileWatcher.start();
            
        } catch (Exception e) {
            logger.error("❌ File watcher test failed", e);
        } finally {
            try {
                if (validatorConsumer != null) validatorConsumer.stop();
                if (transformerConsumer != null) transformerConsumer.stop();
                if (fileWatcher != null) fileWatcher.stop();
                executorService.shutdownNow();
                DatabaseConfig.getInstance().close();
                RabbitMQConfig.getInstance().closeConnection();
            } catch (Exception e) {
                logger.error("Cleanup error", e);
            }
        }
    }
}