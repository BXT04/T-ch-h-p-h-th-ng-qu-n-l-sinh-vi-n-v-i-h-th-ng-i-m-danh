package com.student.integration.watcher;

import com.student.integration.config.ConfigLoader;
import com.student.integration.producer.StudentProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * File Watcher để tự động xử lý CSV files mới
 * CORE FEATURE: "Khi thêm bất kì file CSV sinh viên nào vào để thực hiện check rule"
 */
public class CSVFileWatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(CSVFileWatcher.class);
    
    private final Path watchDirectory;
    private final StudentProducer producer;
    private volatile boolean running = false;
    
    public CSVFileWatcher() {
        ConfigLoader config = ConfigLoader.getInstance();
        String inputDir = config.getProperty("csv.input.directory", "./data/input");
        this.watchDirectory = Paths.get(inputDir);
        this.producer = new StudentProducer();
        
        // Create directories
        try {
            Files.createDirectories(watchDirectory);
            Files.createDirectories(watchDirectory.resolve("processed"));
            Files.createDirectories(watchDirectory.resolve("failed"));
            
            logger.info("✅ Watching directory: {}", watchDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create watch directory", e);
        }
    }
    
    /**
     * Start watching for new CSV files
     */
    public void start() throws IOException, InterruptedException {
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║         REALTIME FILE WATCHER STARTED                ║");
        logger.info("╠══════════════════════════════════════════════════════╣");
        logger.info("║ 📂 Watch folder: {}", watchDirectory.toAbsolutePath());
        logger.info("║ 📝 Drop CSV files here for automatic processing      ║");
        logger.info("║ ⚡ Files will be validated and loaded to DB           ║");
        logger.info("║ ⏹️  Press Ctrl+C to stop                              ║");
        logger.info("╚══════════════════════════════════════════════════════╝\n");
        
        WatchService watchService = FileSystems.getDefault().newWatchService();
        watchDirectory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE
        );
        
        running = true;
        
        while (running) {
            WatchKey key;
            try {
                // Wait for events
                key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;
                
            } catch (InterruptedException e) {
                logger.info("File watcher interrupted");
                break;
            }
            
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path filePath = watchDirectory.resolve(filename);
                
                // Only process CSV files
                if (filename.toString().toLowerCase().endsWith(".csv")) {
                    logger.info("\n🆕 NEW FILE DETECTED: {}", filename);
                    
                    // Wait a bit to ensure file is fully written
                    Thread.sleep(1000);
                    
                    // Process file
                    processCSVFile(filePath);
                }
            }
            
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
        
        watchService.close();
        logger.info("File watcher stopped");
    }
    
    /**
     * Process detected CSV file
     */
    private void processCSVFile(Path csvFile) {
        try {
            if (!Files.exists(csvFile)) {
                logger.warn("⚠️  File no longer exists: {}", csvFile);
                return;
            }
            
            long fileSize = Files.size(csvFile);
            logger.info("📂 Processing file: {}", csvFile.getFileName());
            logger.info("   Size: {} bytes ({} KB)", fileSize, fileSize / 1024);
            
            // Publish to RabbitMQ (streaming mode for realtime)
            long startTime = System.currentTimeMillis();
            
            logger.info("📤 Publishing to RabbitMQ queue...");
            producer.publishFromCSVStreaming(csvFile);
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("✅ SUCCESSFULLY PUBLISHED in {:.2f} seconds", duration / 1000.0);
            logger.info("   ⚡ Messages sent to validation queue");
            logger.info("   🔄 Validators will process automatically");
            logger.info("   💾 Valid data will appear in Clean DB shortly\n");
            
            // Move to processed folder
            Path processedDir = watchDirectory.resolve("processed");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String newFileName = timestamp + "_" + csvFile.getFileName().toString();
            Path target = processedDir.resolve(newFileName);
            
            Files.move(csvFile, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("📁 File moved to: processed/{}\n", newFileName);
            
        } catch (Exception e) {
            logger.error("❌ ERROR processing file {}: {}", csvFile, e.getMessage(), e);
            
            // Move to failed folder
            try {
                Path failedDir = watchDirectory.resolve("failed");
                String timestamp = String.valueOf(System.currentTimeMillis());
                String newFileName = timestamp + "_" + csvFile.getFileName().toString();
                Path target = failedDir.resolve(newFileName);
                Files.move(csvFile, target, StandardCopyOption.REPLACE_EXISTING);
                logger.info("📁 Failed file moved to: failed/{}\n", newFileName);
            } catch (IOException ex) {
                logger.error("Failed to move error file", ex);
            }
        }
    }
    
    /**
     * Stop watching
     */
    public void stop() {
        logger.info("\n⚠️  Stopping file watcher...");
        running = false;
    }
}