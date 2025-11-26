package com.student.integration.producer;

import com.student.integration.model.dto.StudentRawDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Reader để đọc file sinh viên
 * Support streaming processing (đọc từng dòng)
 */
public class CSVReader {
    
    private static final Logger logger = LoggerFactory.getLogger(CSVReader.class);
    
    /**
     * Đọc CSV file và convert thành list StudentRawDTO
     * 
     * @param filePath Path to CSV file
     * @return List of StudentRawDTO
     */
    public List<StudentRawDTO> readCSV(Path filePath) throws IOException {
        List<StudentRawDTO> students = new ArrayList<>();
        
        logger.info("Reading CSV file: {}", filePath.getFileName());
        
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, 
                 CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {
            
            int rowNumber = 1; // Header is row 0
            int processedCount = 0;
            
            for (CSVRecord record : csvParser) {
                rowNumber++;
                
                try {
                    StudentRawDTO student = parseRecord(record, filePath.getFileName().toString(), rowNumber);
                    students.add(student);
                    processedCount++;
                    
                    // Progress log
                    if (processedCount % 1000 == 0) {
                        logger.info("Processed {} records from {}", processedCount, filePath.getFileName());
                    }
                    
                } catch (Exception e) {
                    logger.warn("Error parsing row {}: {}", rowNumber, e.getMessage());
                    // Continue processing next rows
                }
            }
            
            logger.info("✅ Successfully read {} records from {}", processedCount, filePath.getFileName());
            return students;
            
        } catch (IOException e) {
            logger.error("❌ Failed to read CSV file: {}", filePath, e);
            throw e;
        }
    }
    
    /**
     * Đọc CSV file với callback (streaming)
     * Cho phép process từng record ngay khi đọc
     * 
     * @param filePath Path to CSV file
     * @param callback Callback để xử lý mỗi record
     */
    public void readCSVStreaming(Path filePath, RecordCallback callback) throws IOException {
        
        logger.info("Reading CSV file (streaming mode): {}", filePath.getFileName());
        
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, 
                 CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {
            
            int rowNumber = 1;
            int processedCount = 0;
            int errorCount = 0;
            
            for (CSVRecord record : csvParser) {
                rowNumber++;
                
                try {
                    StudentRawDTO student = parseRecord(record, filePath.getFileName().toString(), rowNumber);
                    
                    // Callback để xử lý record (publish to queue)
                    callback.onRecord(student, processedCount + 1);
                    processedCount++;
                    
                    // Progress log
                    if (processedCount % 100 == 0) {
                        logger.info("📤 Processed {} records (Errors: {})", processedCount, errorCount);
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                    logger.warn("Error processing row {}: {}", rowNumber, e.getMessage());
                    callback.onError(rowNumber, e);
                }
            }
            
            logger.info("✅ Streaming completed: {} records processed, {} errors", 
                processedCount, errorCount);
            
            // Final callback
            callback.onComplete(processedCount, errorCount);
            
        } catch (IOException e) {
            logger.error("❌ Failed to read CSV file: {}", filePath, e);
            throw e;
        }
    }
    
    /**
     * Parse CSVRecord thành StudentRawDTO
     */
    private StudentRawDTO parseRecord(CSVRecord record, String sourceFile, int rowNumber) {
        StudentRawDTO student = new StudentRawDTO();
        
        // Basic Information
        student.setStudentId(getFieldValue(record, "student_id"));
        student.setFullName(getFieldValue(record, "full_name"));
        student.setDateOfBirth(getFieldValue(record, "date_of_birth"));
        student.setGender(getFieldValue(record, "gender"));
        
        // Contact Information
        student.setEmail(getFieldValue(record, "email"));
        student.setPhone(getFieldValue(record, "phone"));
        student.setAddress(getFieldValue(record, "address"));
        student.setCity(getFieldValue(record, "city"));
        student.setProvince(getFieldValue(record, "province"));
        student.setPostalCode(getFieldValue(record, "postal_code"));
        
        // Academic Information
        student.setClassCode(getFieldValue(record, "class_code"));
        student.setMajor(getFieldValue(record, "major"));
        student.setFaculty(getFieldValue(record, "faculty"));
        student.setAcademicYear(getFieldValue(record, "academic_year"));
        student.setEnrollmentDate(getFieldValue(record, "enrollment_date"));
        
        // Performance
        student.setGpa(getFieldValue(record, "gpa"));
        student.setTotalCredits(getFieldValue(record, "total_credits"));
        student.setStatus(getFieldValue(record, "status"));
        
        // Metadata
        student.setSourceFile(sourceFile);
        student.setRowNum(rowNumber);
        
        return student;
    }
    
    /**
     * Helper: Get field value từ CSV record (handle missing columns)
     */
    private String getFieldValue(CSVRecord record, String fieldName) {
        try {
            if (record.isMapped(fieldName)) {
                return record.get(fieldName);
            }
        } catch (IllegalArgumentException e) {
            // Column not found
        }
        return null;
    }
    
    /**
     * Callback interface cho streaming mode
     */
    public interface RecordCallback {
        void onRecord(StudentRawDTO student, int recordNumber) throws Exception;
        void onError(int rowNumber, Exception error);
        void onComplete(int totalProcessed, int totalErrors);
    }
}