package com.student.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Helper class để publish messages vào RabbitMQ
 */
public class MessagePublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);
    
    private final RabbitMQConfig config;
    private final QueueManager queueManager;
    private final ObjectMapper objectMapper;
    
    public MessagePublisher() {
        this.config = RabbitMQConfig.getInstance();
        this.queueManager = new QueueManager();
        
        // Configure ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Publish object as JSON message
     */
    public void publish(String exchange, String routingKey, Object message) 
            throws IOException, TimeoutException {
        
        Connection connection = config.getConnection();
        
        try (Channel channel = connection.createChannel()) {
            
            // Serialize object to JSON
            String jsonMessage = objectMapper.writeValueAsString(message);
            byte[] messageBytes = jsonMessage.getBytes("UTF-8");
            
            // Publish with persistent delivery mode
            channel.basicPublish(
                exchange,
                routingKey,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                messageBytes
            );
            
            logger.debug("Published message to {}/{}: {} bytes", 
                exchange, routingKey, messageBytes.length);
            
        } catch (IOException e) {
            logger.error("Failed to publish message to {}/{}", exchange, routingKey, e);
            throw e;
        }
    }
    
    /**
     * Publish to RAW queue
     */
    public void publishToRaw(Object message) throws IOException, TimeoutException {
        publish(
            queueManager.getExchangeName(),
            queueManager.getRoutingKeyRaw(),
            message
        );
    }
    
    /**
     * Publish to VALIDATED queue
     */
    public void publishToValidated(Object message) throws IOException, TimeoutException {
        publish(
            queueManager.getExchangeName(),
            queueManager.getRoutingKeyValidated(),
            message
        );
    }
    
    /**
     * Publish to TRANSFORMED queue
     */
    public void publishToTransformed(Object message) throws IOException, TimeoutException {
        publish(
            queueManager.getExchangeName(),
            queueManager.getRoutingKeyTransformed(),
            message
        );
    }
    
    /**
     * Publish to ERROR queue
     */
    public void publishToError(Object message) throws IOException, TimeoutException {
        publish(
            queueManager.getExchangeName(),
            queueManager.getRoutingKeyError(),
            message
        );
    }
    
    /**
     * Publish batch messages
     */
    public void publishBatch(String exchange, String routingKey, Iterable<?> messages) 
            throws IOException, TimeoutException {
        
        Connection connection = config.getConnection();
        
        try (Channel channel = connection.createChannel()) {
            
            int count = 0;
            for (Object message : messages) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                byte[] messageBytes = jsonMessage.getBytes("UTF-8");
                
                channel.basicPublish(
                    exchange,
                    routingKey,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messageBytes
                );
                
                count++;
            }
            
            logger.info("Published {} messages to {}/{}", count, exchange, routingKey);
            
        } catch (IOException e) {
            logger.error("Failed to publish batch messages", e);
            throw e;
        }
    }
}