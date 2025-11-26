package com.student.integration.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Singleton class quản lý RabbitMQ connection
 * Đảm bảo chỉ có 1 connection duy nhất trong toàn bộ application
 */
public class RabbitMQConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    
    private static RabbitMQConfig instance;
    private Connection connection;
    private ConnectionFactory factory;
    
    // Configuration
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String virtualHost;
    
    /**
     * Private constructor cho Singleton pattern
     */
    private RabbitMQConfig() {
        // Load từ application.properties
        ConfigLoader config = ConfigLoader.getInstance();
        
        this.host = config.getProperty("rabbitmq.host", "localhost");
        this.port = Integer.parseInt(config.getProperty("rabbitmq.port", "5672"));
        this.username = config.getProperty("rabbitmq.username", "admin");
        this.password = config.getProperty("rabbitmq.password", "admin123");
        this.virtualHost = config.getProperty("rabbitmq.virtualhost", "/");
        
        initializeFactory();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized RabbitMQConfig getInstance() {
        if (instance == null) {
            instance = new RabbitMQConfig();
        }
        return instance;
    }
    
    /**
     * Initialize ConnectionFactory
     */
    private void initializeFactory() {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        
        // Connection settings
        factory.setConnectionTimeout(30000);      // 30 seconds
        factory.setRequestedHeartbeat(60);        // 60 seconds
        factory.setAutomaticRecoveryEnabled(true); // Auto-reconnect
        factory.setNetworkRecoveryInterval(10000); // 10 seconds
        
        logger.info("RabbitMQ ConnectionFactory initialized - {}:{}", host, port);
    }
    
    /**
     * Get hoặc tạo mới connection
     */
    public synchronized Connection getConnection() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            logger.info("Creating new RabbitMQ connection...");
            connection = factory.newConnection("StudentIntegrationSystem");
            logger.info("✅ RabbitMQ connection established successfully");
        }
        return connection;
    }
    
    /**
     * Close connection
     */
    public synchronized void closeConnection() {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
                logger.info("RabbitMQ connection closed");
            } catch (IOException e) {
                logger.error("Error closing RabbitMQ connection", e);
            }
        }
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        try {
            Connection testConn = getConnection();
            boolean isOpen = testConn.isOpen();
            logger.info("Connection test: {}", isOpen ? "SUCCESS" : "FAILED");
            return isOpen;
        } catch (Exception e) {
            logger.error("Connection test FAILED: {}", e.getMessage());
            return false;
        }
    }
    
    // Getters
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getUsername() {
        return username;
    }
}