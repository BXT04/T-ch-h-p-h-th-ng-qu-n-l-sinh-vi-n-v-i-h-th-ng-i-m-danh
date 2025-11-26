package com.student.integration.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.student.integration.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Base class cho tất cả consumers
 * Provide common functionality: connection, channel, JSON parsing
 */
public abstract class BaseConsumer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper;
    protected final RabbitMQConfig config;
    
    protected Channel channel;
    protected volatile boolean running = false;
    
    protected BaseConsumer() {
        this.config = RabbitMQConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDateTime support
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        this.objectMapper.registerModule(javaTimeModule);
        
        // Configure ObjectMapper to handle timestamps properly
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        this.objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    }
    
    /**
     * Start consuming messages từ queue
     */
    public void startConsuming(String queueName) throws IOException, TimeoutException {
        startConsuming(queueName, 10); // Default prefetch = 10
    }
    
    /**
     * Start consuming với custom prefetch count
     */
    public void startConsuming(String queueName, int prefetchCount) throws IOException, TimeoutException {
        logger.info("🚀 Starting consumer for queue: {}", queueName);
        logger.info("   Prefetch count: {}", prefetchCount);
        
        // Get connection và create channel
        Connection connection = config.getConnection();
        channel = connection.createChannel();
        
        // Set QoS - Prefetch count
        channel.basicQos(prefetchCount);
        
        running = true;
        
        // Define callback
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                // Process message
                processMessage(delivery.getBody());
                
                // Manual ACK sau khi xử lý xong
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                
            } catch (Exception e) {
                logger.error("Error processing message", e);
                
                // NACK - requeue nếu có lỗi
                try {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                } catch (IOException ex) {
                    logger.error("Error sending NACK", ex);
                }
            }
        };
        
        // Start consuming
        channel.basicConsume(
            queueName,
            false,              // autoAck = false (manual ACK)
            deliverCallback,
            consumerTag -> logger.warn("Consumer cancelled: {}", consumerTag)
        );
        
        logger.info("✅ Consumer started. Waiting for messages...");
        logger.info("   Press Ctrl+C to stop");
    }
    
    /**
     * Template method - subclass implement
     */
    protected abstract void processMessage(byte[] messageBody) throws Exception;
    
    /**
     * Stop consumer
     */
    public void stop() throws IOException, TimeoutException {
        logger.info("⏹️  Stopping consumer...");
        running = false;
        
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        
        logger.info("✅ Consumer stopped");
    }
    
    /**
     * Wait until interrupted (for main thread)
     */
    public void waitUntilInterrupted() {
        try {
            while (running) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.info("Consumer interrupted");
            Thread.currentThread().interrupt();
        }
    }
}