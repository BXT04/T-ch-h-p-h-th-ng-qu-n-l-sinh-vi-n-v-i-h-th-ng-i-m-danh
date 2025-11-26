package com.student.integration.loader;

import com.student.integration.config.DatabaseConfig;
import com.student.integration.model.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader để insert/update students vào Clean Database
 */
public class StudentLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentLoader.class);
    private final DatabaseConfig dbConfig;
    
    // Cache: class_code → class_id
    private final Map<String, Integer> classIdCache = new HashMap<>();
    
    public StudentLoader() {
        this.dbConfig = DatabaseConfig.getInstance();
        loadClassIdCache();
    }
    
    /**
     * Load class_id cache từ database
     */
    private void loadClassIdCache() {
        String sql = "SELECT id, class_code FROM classes";
        
        try (Connection conn = dbConfig.getCleanConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                classIdCache.put(rs.getString("class_code"), rs.getInt("id"));
            }
            
            logger.info("✅ Loaded {} class codes into cache", classIdCache.size());
            
        } catch (SQLException e) {
            logger.error("Failed to load class ID cache", e);
        }
    }
    
    /**
     * Get class_id by class_code
     */
    public Integer getClassId(String classCode) {
        return classIdCache.get(classCode);
    }
    
    /**
     * Insert single student (with UPSERT logic)
     */
    public void insertStudent(Student student) throws SQLException {
        String sql = """
            INSERT INTO students (
                student_id, full_name, date_of_birth, gender,
                email, phone_number, address,
                class_id, department_id, enrollment_year, gpa, credits_completed, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                full_name = VALUES(full_name),
                date_of_birth = VALUES(date_of_birth),
                gender = VALUES(gender),
                email = VALUES(email),
                phone_number = VALUES(phone_number),
                address = VALUES(address),
                class_id = VALUES(class_id),
                department_id = VALUES(department_id),
                enrollment_year = VALUES(enrollment_year),
                gpa = VALUES(gpa),
                credits_completed = VALUES(credits_completed),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
        
        try (Connection conn = dbConfig.getCleanConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setStudentParameters(pstmt, student);
            pstmt.executeUpdate();
            
            logger.debug("Inserted/Updated student: {}", student.getStudentId());
            
        } catch (SQLException e) {
            logger.error("Failed to insert student {}", student.getStudentId(), e);
            throw e;
        }
    }
    
    /**
     * Batch insert students (performance optimization)
     */
    public int insertStudentBatch(List<Student> students) throws SQLException {
        if (students == null || students.isEmpty()) {
            return 0;
        }
        
        String sql = """
            INSERT INTO students (
                student_id, full_name, date_of_birth, gender,
                email, phone_number, address,
                class_id, department_id, enrollment_year, gpa, credits_completed, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                full_name = VALUES(full_name),
                date_of_birth = VALUES(date_of_birth),
                gender = VALUES(gender),
                email = VALUES(email),
                phone_number = VALUES(phone_number),
                address = VALUES(address),
                class_id = VALUES(class_id),
                department_id = VALUES(department_id),
                enrollment_year = VALUES(enrollment_year),
                gpa = VALUES(gpa),
                credits_completed = VALUES(credits_completed),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbConfig.getCleanConnection();
            conn.setAutoCommit(false); // Start transaction
            
            pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (Student student : students) {
                setStudentParameters(pstmt, student);
                pstmt.addBatch();
                count++;
                
                // Execute batch mỗi 100 records
                if (count % 100 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    logger.debug("Committed batch of 100 students");
                }
            }
            
            // Execute remaining
            pstmt.executeBatch();
            conn.commit();
            
            logger.info("✅ Batch inserted {} students", count);
            return count;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Transaction rolled back");
                } catch (SQLException ex) {
                    logger.error("Rollback failed", ex);
                }
            }
            throw e;
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Set prepared statement parameters
     */
    private void setStudentParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        pstmt.setString(2, student.getFullName());
        pstmt.setDate(3, student.getDateOfBirth() != null ? Date.valueOf(student.getDateOfBirth()) : null);
        pstmt.setString(4, student.getGender() != null ? student.getGender().name() : null);
        pstmt.setString(5, student.getEmail());
        pstmt.setString(6, student.getPhone()); // phone_number in DB
        pstmt.setString(7, student.getAddress());
        pstmt.setInt(8, student.getClassId());
        
        // department_id: use default 1 (Information Technology)
        pstmt.setInt(9, 1);
        
        // enrollment_year: extract year from enrollmentDate or use default
        if (student.getEnrollmentDate() != null) {
            pstmt.setInt(10, student.getEnrollmentDate().getYear());
        } else {
            pstmt.setInt(10, 2021); // Default year
        }
        
        pstmt.setBigDecimal(11, student.getGpa());
        pstmt.setInt(12, student.getTotalCredits()); // credits_completed in DB
        pstmt.setString(13, student.getStatus() != null ? student.getStatus().name() : null);
    }
    
    /**
     * Count total students in clean DB
     */
    public int countStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        
        try (Connection conn = dbConfig.getCleanConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}