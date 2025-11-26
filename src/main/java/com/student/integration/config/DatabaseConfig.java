package com.student.integration.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database configuration với HikariCP connection pooling
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static DatabaseConfig instance;
    private HikariDataSource rawDataSource;
    private HikariDataSource cleanDataSource;
    
    private DatabaseConfig() {
        initializeDataSources();
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    /**
     * Initialize both Raw and Clean datasources
     */
    private void initializeDataSources() {
        ConfigLoader config = ConfigLoader.getInstance();
        
        // Raw Database
        HikariConfig rawConfig = new HikariConfig();
        rawConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            config.getProperty("db.raw.host", "localhost"),
            config.getProperty("db.raw.port", "3307"),
            config.getProperty("db.raw.name", "student_raw_db")));
        rawConfig.setUsername(config.getProperty("db.raw.username", "raw_user"));
        rawConfig.setPassword(config.getProperty("db.raw.password", "raw_pass"));
        rawConfig.setMaximumPoolSize(config.getIntProperty("db.pool.maximumPoolSize", 10));
        rawConfig.setMinimumIdle(config.getIntProperty("db.pool.minimumIdle", 2));
        rawConfig.setConnectionTimeout(config.getIntProperty("db.pool.connectionTimeout", 30000));
        rawConfig.setPoolName("RawDB-Pool");
        
        rawDataSource = new HikariDataSource(rawConfig);
        logger.info("✅ Raw Database connection pool initialized");
        
        // Clean Database
        HikariConfig cleanConfig = new HikariConfig();
        cleanConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            config.getProperty("db.clean.host", "localhost"),
            config.getProperty("db.clean.port", "3308"),
            config.getProperty("db.clean.name", "student_clean_db")));
        cleanConfig.setUsername(config.getProperty("db.clean.username", "clean_user"));
        cleanConfig.setPassword(config.getProperty("db.clean.password", "clean_pass"));
        cleanConfig.setMaximumPoolSize(config.getIntProperty("db.pool.maximumPoolSize", 10));
        cleanConfig.setMinimumIdle(config.getIntProperty("db.pool.minimumIdle", 2));
        cleanConfig.setConnectionTimeout(config.getIntProperty("db.pool.connectionTimeout", 30000));
        cleanConfig.setPoolName("CleanDB-Pool");
        
        cleanDataSource = new HikariDataSource(cleanConfig);
        logger.info("✅ Clean Database connection pool initialized");
    }
    
    /**
     * Get connection to Raw Database
     */
    public Connection getRawConnection() throws SQLException {
        return rawDataSource.getConnection();
    }
    
    /**
     * Get connection to Clean Database
     */
    public Connection getCleanConnection() throws SQLException {
        return cleanDataSource.getConnection();
    }
    
    /**
     * Get Raw DataSource
     */
    public DataSource getRawDataSource() {
        return rawDataSource;
    }
    
    /**
     * Get Clean DataSource
     */
    public DataSource getCleanDataSource() {
        return cleanDataSource;
    }
    
    /**
     * Test connections
     */
    public boolean testConnections() {
        boolean rawOk = false;
        boolean cleanOk = false;
        
        try (Connection conn = getRawConnection()) {
            rawOk = conn.isValid(5);
            logger.info("Raw DB connection test: {}", rawOk ? "SUCCESS" : "FAILED");
        } catch (SQLException e) {
            logger.error("Raw DB connection test FAILED", e);
        }
        
        try (Connection conn = getCleanConnection()) {
            cleanOk = conn.isValid(5);
            logger.info("Clean DB connection test: {}", cleanOk ? "SUCCESS" : "FAILED");
        } catch (SQLException e) {
            logger.error("Clean DB connection test FAILED", e);
        }
        
        return rawOk && cleanOk;
    }
    
    /**
     * Close all connections
     */
    public void close() {
        if (rawDataSource != null && !rawDataSource.isClosed()) {
            rawDataSource.close();
            logger.info("Raw Database connection pool closed");
        }
        
        if (cleanDataSource != null && !cleanDataSource.isClosed()) {
            cleanDataSource.close();
            logger.info("Clean Database connection pool closed");
        }
    }
}