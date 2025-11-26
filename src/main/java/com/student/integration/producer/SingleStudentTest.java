package com.student.integration.producer;

import com.student.integration.config.QueueManager;
import com.student.integration.config.RabbitMQConfig;
import com.student.integration.model.dto.StudentRawDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test adding a single new student (Realtime requirement)
 */
public class SingleStudentTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SingleStudentTest.class);
    
    public static void main(String[] args) {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║     SINGLE STUDENT PUBLISH TEST              ║");
        logger.info("║  (Test yêu cầu: Thêm sinh viên realtime)     ║");
        logger.info("╚══════════════════════════════════════════════╝\n");
        
        try {
            // Setup
            QueueManager queueManager = new QueueManager();
            queueManager.setupQueues();
            
            // Create new student
            StudentRawDTO newStudent = new StudentRawDTO();
            newStudent.setStudentId("SV20250001");
            newStudent.setFullName("Nguyen Van Test");
            newStudent.setDateOfBirth("2003-01-15");
            newStudent.setGender("Male");
            newStudent.setEmail("nguyenvantest@student.edu.vn");
            newStudent.setPhone("0901234567");
            newStudent.setAddress("123 Test Street");
            newStudent.setCity("Da Nang");
            newStudent.setProvince("Da Nang");
            newStudent.setPostalCode("50000");
            newStudent.setClassCode("SE01K01");
            newStudent.setMajor("Software Engineering");
            newStudent.setFaculty("Information Technology");
            newStudent.setAcademicYear("2024-2025");
            newStudent.setEnrollmentDate("2024-09-01");
            newStudent.setGpa("3.50");
            newStudent.setTotalCredits("120");
            newStudent.setStatus("ACTIVE");
            newStudent.setSourceFile("manual_input");
            newStudent.setRowNum(1);
            
            // Publish
            logger.info("📤 Publishing new student: {}", newStudent.getStudentId());
            StudentProducer producer = new StudentProducer();
            producer.publishSingleStudent(newStudent);
            
            logger.info("✅ Student published to queue!");
            logger.info("🔍 Check RabbitMQ: http://localhost:15672");
            logger.info("   Queue 'student.raw' should have +1 message");
            
            // Cleanup
            RabbitMQConfig.getInstance().closeConnection();
            
        } catch (Exception e) {
            logger.error("❌ Test failed", e);
        }
    }
}