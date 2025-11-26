package com.student.integration.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Class quản lý queues, exchanges, và bindings trong RabbitMQ
 */
public class QueueManager {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
    
    private final RabbitMQConfig config;
    private final ConfigLoader configLoader;
    
    // Queue names
    private final String queueRaw;
    private final String queueValidated;
    private final String queueTransformed;
    private final String queueError;
    
    // Exchange
    private final String exchangeName;
    
    // Routing keys
    private final String routingKeyRaw;
    private final String routingKeyValidated;
    private final String routingKeyTransformed;
    private final String routingKeyError;
    
    public QueueManager() {
        this.config = RabbitMQConfig.getInstance();
        this.configLoader = ConfigLoader.getInstance();
        
        // Load configuration
        this.queueRaw = configLoader.getProperty("queue.student.raw", "student.raw");
        this.queueValidated = configLoader.getProperty("queue.student.validated", "student.validated");
        this.queueTransformed = configLoader.getProperty("queue.student.transformed", "student.transformed");
        this.queueError = configLoader.getProperty("queue.student.error", "student.error");
        
        this.exchangeName = configLoader.getProperty("exchange.student", "student.exchange");
        
        this.routingKeyRaw = configLoader.getProperty("routing.key.raw", "student.raw");
        this.routingKeyValidated = configLoader.getProperty("routing.key.validated", "student.validated");
        this.routingKeyTransformed = configLoader.getProperty("routing.key.transformed", "student.transformed");
        this.routingKeyError = configLoader.getProperty("routing.key.error", "student.error");
    }
    
    /**
     * Setup tất cả queues, exchanges, và bindings
     */
    public void setupQueues() throws IOException, TimeoutException {
        logger.info("Setting up RabbitMQ queues and exchanges...");
        
        Connection connection = config.getConnection();
        
        try (Channel channel = connection.createChannel()) {
            
            // 1. Declare Exchange (Direct type)
            channel.exchangeDeclare(
                exchangeName,
                "direct",     // Exchange type
                true,         // Durable
                false,        // Auto-delete
                null          // Arguments
            );
            logger.info("✅ Exchange declared: {}", exchangeName);
            
            // 2. Declare Queues với properties
            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-message-ttl", 86400000); // 24 hours TTL
            queueArgs.put("x-max-length", 100000);    // Max 100k messages
            
            // Queue: student.raw
            channel.queueDeclare(
                queueRaw,
                true,      // Durable
                false,     // Exclusive
                false,     // Auto-delete
                queueArgs  // Arguments
            );
            channel.queueBind(queueRaw, exchangeName, routingKeyRaw);
            logger.info("✅ Queue declared and bound: {} → {}", queueRaw, routingKeyRaw);
            
            // Queue: student.validated
            channel.queueDeclare(queueValidated, true, false, false, queueArgs);
            channel.queueBind(queueValidated, exchangeName, routingKeyValidated);
            logger.info("✅ Queue declared and bound: {} → {}", queueValidated, routingKeyValidated);
            
            // Queue: student.transformed
            channel.queueDeclare(queueTransformed, true, false, false, queueArgs);
            channel.queueBind(queueTransformed, exchangeName, routingKeyTransformed);
            logger.info("✅ Queue declared and bound: {} → {}", queueTransformed, routingKeyTransformed);
            
            // Queue: student.error (Special handling)
            Map<String, Object> errorQueueArgs = new HashMap<>();
            errorQueueArgs.put("x-message-ttl", 604800000); // 7 days TTL for errors
            channel.queueDeclare(queueError, true, false, false, errorQueueArgs);
            channel.queueBind(queueError, exchangeName, routingKeyError);
            logger.info("✅ Queue declared and bound: {} → {}", queueError, routingKeyError);
            
            logger.info("🎉 All queues and exchanges setup completed!");
            
            // Print summary
            printQueueSummary(channel);
            
        } catch (IOException e) {
            logger.error("❌ Failed to setup queues", e);
            throw e;
        }
    }
    
    /**
     * Print queue summary (message counts)
     */
    private void printQueueSummary(Channel channel) {
        try {
            logger.info("\n╔════════════════════════════════════════╗");
            logger.info("║        QUEUE SUMMARY                   ║");
            logger.info("╠════════════════════════════════════════╣");
            
            printQueueInfo(channel, queueRaw);
            printQueueInfo(channel, queueValidated);
            printQueueInfo(channel, queueTransformed);
            printQueueInfo(channel, queueError);
            
            logger.info("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            logger.warn("Could not fetch queue info", e);
        }
    }
    
    private void printQueueInfo(Channel channel, String queueName) throws IOException {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queueName);
        logger.info("║ {}: {} messages", 
            String.format("%-25s", queueName), 
            declareOk.getMessageCount());
    }
    
    /**
     * Purge tất cả queues (Xóa tất cả messages)
     */
    public void purgeAllQueues() throws IOException, TimeoutException {
        logger.warn("⚠️  Purging all queues...");
        
        Connection connection = config.getConnection();
        
        try (Channel channel = connection.createChannel()) {
            channel.queuePurge(queueRaw);
            channel.queuePurge(queueValidated);
            channel.queuePurge(queueTransformed);
            channel.queuePurge(queueError);
            
            logger.info("✅ All queues purged");
        }
    }
    
    /**
     * Delete tất cả queues
     */
    public void deleteAllQueues() throws IOException, TimeoutException {
        logger.warn("⚠️  Deleting all queues...");
        
        Connection connection = config.getConnection();
        
        try (Channel channel = connection.createChannel()) {
            channel.queueDelete(queueRaw);
            channel.queueDelete(queueValidated);
            channel.queueDelete(queueTransformed);
            channel.queueDelete(queueError);
            channel.exchangeDelete(exchangeName);
            
            logger.info("✅ All queues and exchange deleted");
        }
    }
    
    // Getters
    public String getQueueRaw() {
        return queueRaw;
    }
    
    public String getQueueValidated() {
        return queueValidated;
    }
    
    public String getQueueTransformed() {
        return queueTransformed;
    }
    
    public String getQueueError() {
        return queueError;
    }
    
    public String getExchangeName() {
        return exchangeName;
    }
    
    public String getRoutingKeyRaw() {
        return routingKeyRaw;
    }
    
    public String getRoutingKeyValidated() {
        return routingKeyValidated;
    }
    
    public String getRoutingKeyTransformed() {
        return routingKeyTransformed;
    }
    
    public String getRoutingKeyError() {
        return routingKeyError;
    }
}