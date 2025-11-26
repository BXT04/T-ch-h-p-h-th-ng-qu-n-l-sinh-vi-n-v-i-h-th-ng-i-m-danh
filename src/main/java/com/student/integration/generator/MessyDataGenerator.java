package com.student.integration.generator;

import com.github.javafaker.Faker;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generator để tạo 20,000 dữ liệu sinh viên với các lỗi thực tế
 * Phục vụ testing cho validation framework
 */
public class MessyDataGenerator {

    private static final Faker faker = new Faker(new Locale("vi"));
    private static final Random random = new Random();
    
    // Constants
    private static final int TOTAL_RECORDS = 20000;
    private static final double ERROR_RATE = 0.20; // 20% dữ liệu có lỗi
    
    // Validation formats
    private static final String[] CITIES = {"Da Nang", "Ha Noi", "Ho Chi Minh", "Hue", "Can Tho", "Hai Phong"};
    private static final String[] PROVINCES = {"Da Nang", "Ha Noi", "Ho Chi Minh", "Thua Thien Hue", "Can Tho", "Hai Phong"};
    private static final String[] MAJORS = {"Software Engineering", "Artificial Intelligence", "Data Science", 
                                            "Computer Science", "Information Systems", "Computer Engineering"};
    private static final String[] FACULTIES = {"Information Technology", "Business Administration", 
                                               "Engineering", "Natural Sciences"};
    private static final String[] CLASS_CODES = {"SE01K01", "SE01K02", "AI01K01", "DS01K01", "CS01K01", "IS01K01"};
    private static final String[] STATUSES = {"ACTIVE", "INACTIVE", "SUSPENDED"};
    
    // Error types distribution
    private enum ErrorType {
        INVALID_STUDENT_ID(3.0),
        INVALID_EMAIL(4.0),
        INVALID_PHONE(3.0),
        INVALID_GPA(2.0),
        INVALID_DOB(3.0),
        MISSING_FIELDS(3.0),
        INVALID_CLASS_CODE(2.0);
        
        final double percentage;
        ErrorType(double percentage) {
            this.percentage = percentage;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== MESSY DATA GENERATOR ===");
            System.out.println("Tổng số records: " + TOTAL_RECORDS);
            System.out.println("Tỷ lệ lỗi: " + (ERROR_RATE * 100) + "%");
            System.out.println("Bắt đầu generate...\n");
            
            // Tạo thư mục output nếu chưa có
            Path outputDir = Paths.get("./data/generated");
            Files.createDirectories(outputDir);
            
            String outputFile = outputDir.resolve("students_messy_20k.csv").toString();
            
            generateMessyStudentData(outputFile);
            
            System.out.println("\n✅ Hoàn thành! File được lưu tại: " + outputFile);
            System.out.println("Có thể copy file này vào ./data/input để test hệ thống");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate CSV file với dữ liệu lộn xộn
     */
    public static void generateMessyStudentData(String outputFile) throws IOException {
        
        // CSV Headers
        String[] headers = {
            "student_id", "full_name", "date_of_birth", "gender", "email", "phone",
            "address", "city", "province", "postal_code",
            "class_code", "major", "faculty", "academic_year", "enrollment_date",
            "gpa", "total_credits", "status"
        };
        
        try (FileWriter writer = new FileWriter(outputFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {
            
            int errorCount = 0;
            int correctCount = 0;
            
            // Tính số lượng errors cho mỗi loại
            Map<ErrorType, Integer> errorDistribution = calculateErrorDistribution();
            Map<ErrorType, Integer> errorCounters = new HashMap<>();
            for (ErrorType type : ErrorType.values()) {
                errorCounters.put(type, 0);
            }
            
            for (int i = 1; i <= TOTAL_RECORDS; i++) {
                
                // Quyết định record này có lỗi không
                boolean shouldHaveError = random.nextDouble() < ERROR_RATE;
                ErrorType errorType = null;
                
                if (shouldHaveError) {
                    // Chọn loại lỗi dựa trên distribution
                    errorType = selectErrorType(errorDistribution, errorCounters);
                    if (errorType != null) {
                        errorCount++;
                        errorCounters.put(errorType, errorCounters.get(errorType) + 1);
                    } else {
                        shouldHaveError = false;
                        correctCount++;
                    }
                } else {
                    correctCount++;
                }
                
                // Generate student record
                StudentRecord student = shouldHaveError 
                    ? generateErrorRecord(i, errorType) 
                    : generateCorrectRecord(i);
                
                // Write to CSV
                csvPrinter.printRecord(
                    student.studentId, student.fullName, student.dateOfBirth, student.gender,
                    student.email, student.phone, student.address, student.city, student.province,
                    student.postalCode, student.classCode, student.major, student.faculty,
                    student.academicYear, student.enrollmentDate, student.gpa, student.totalCredits,
                    student.status
                );
                
                // Progress
                if (i % 1000 == 0) {
                    System.out.printf("Generated %d/%d records (%.1f%% - Errors: %d, Correct: %d)\n", 
                        i, TOTAL_RECORDS, (i * 100.0 / TOTAL_RECORDS), errorCount, correctCount);
                }
            }
            
            // Summary
            System.out.println("\n=== SUMMARY ===");
            System.out.println("Total: " + TOTAL_RECORDS);
            System.out.println("Correct: " + correctCount + String.format(" (%.1f%%)", correctCount * 100.0 / TOTAL_RECORDS));
            System.out.println("Errors: " + errorCount + String.format(" (%.1f%%)", errorCount * 100.0 / TOTAL_RECORDS));
            System.out.println("\nError distribution:");
            for (ErrorType type : ErrorType.values()) {
                int count = errorCounters.get(type);
                System.out.printf("  - %s: %d (%.1f%%)\n", type.name(), count, count * 100.0 / TOTAL_RECORDS);
            }
        }
    }

    /**
     * Tính phân bổ số lượng errors cho mỗi loại
     */
    private static Map<ErrorType, Integer> calculateErrorDistribution() {
        Map<ErrorType, Integer> distribution = new HashMap<>();
        int totalErrors = (int) (TOTAL_RECORDS * ERROR_RATE);
        
        double totalPercentage = 0;
        for (ErrorType type : ErrorType.values()) {
            totalPercentage += type.percentage;
        }
        
        for (ErrorType type : ErrorType.values()) {
            int count = (int) ((type.percentage / totalPercentage) * totalErrors);
            distribution.put(type, count);
        }
        
        return distribution;
    }

    /**
     * Chọn error type dựa trên distribution
     */
    private static ErrorType selectErrorType(Map<ErrorType, Integer> distribution, 
                                            Map<ErrorType, Integer> counters) {
        List<ErrorType> available = new ArrayList<>();
        for (ErrorType type : ErrorType.values()) {
            if (counters.get(type) < distribution.get(type)) {
                available.add(type);
            }
        }
        return available.isEmpty() ? null : available.get(random.nextInt(available.size()));
    }

    /**
     * Generate record ĐÚNG
     */
    private static StudentRecord generateCorrectRecord(int index) {
        StudentRecord student = new StudentRecord();
        
        // Student ID: SV + 8 digits (2021 + 4 digits sequence)
        student.studentId = String.format("SV%04d%04d", 2021, index);
        
        // Personal info
        student.fullName = faker.name().fullName();
        student.gender = random.nextBoolean() ? "Male" : "Female";
        
        // Date of birth: 17-25 tuổi (1998-2006)
        LocalDate dob = LocalDate.of(
            ThreadLocalRandom.current().nextInt(2000, 2007),
            ThreadLocalRandom.current().nextInt(1, 13),
            ThreadLocalRandom.current().nextInt(1, 29)
        );
        student.dateOfBirth = dob.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Contact
        String username = student.fullName.toLowerCase()
            .replaceAll("\\s+", "")
            .replaceAll("[^a-z]", "");
        student.email = username + index + "@student.edu.vn";
        student.phone = generateValidPhone();
        
        // Address
        student.address = faker.address().streetAddress();
        student.city = CITIES[random.nextInt(CITIES.length)];
        student.province = student.city; // Simplified
        student.postalCode = String.format("%05d", random.nextInt(100000));
        
        // Academic
        student.classCode = CLASS_CODES[random.nextInt(CLASS_CODES.length)];
        student.major = MAJORS[random.nextInt(MAJORS.length)];
        student.faculty = FACULTIES[random.nextInt(FACULTIES.length)];
        student.academicYear = "2021-2022";
        
        LocalDate enrollmentDate = LocalDate.of(2021, 9, 1);
        student.enrollmentDate = enrollmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Performance
        student.gpa = String.format("%.2f", 2.0 + random.nextDouble() * 2.0); // 2.0-4.0
        student.totalCredits = String.valueOf(ThreadLocalRandom.current().nextInt(80, 140));
        student.status = STATUSES[random.nextInt(STATUSES.length)];
        
        return student;
    }

    /**
     * Generate record CÓ LỖI
     */
    private static StudentRecord generateErrorRecord(int index, ErrorType errorType) {
        StudentRecord student = generateCorrectRecord(index);
        
        switch (errorType) {
            case INVALID_STUDENT_ID:
                student.studentId = generateInvalidStudentId(index);
                break;
                
            case INVALID_EMAIL:
                student.email = generateInvalidEmail(student.fullName, index);
                break;
                
            case INVALID_PHONE:
                student.phone = generateInvalidPhone();
                break;
                
            case INVALID_GPA:
                student.gpa = generateInvalidGPA();
                break;
                
            case INVALID_DOB:
                student.dateOfBirth = generateInvalidDOB();
                break;
                
            case MISSING_FIELDS:
                applyMissingFields(student);
                break;
                
            case INVALID_CLASS_CODE:
                student.classCode = generateInvalidClassCode();
                break;
        }
        
        return student;
    }

    /**
     * Generate invalid Student ID
     */
    private static String generateInvalidStudentId(int index) {
        int errorCase = random.nextInt(5);
        switch (errorCase) {
            case 0: return String.format("SV%03d%04d", 2021, index); // Thiếu 1 digit (7 thay vì 8)
            case 1: return String.format("ST%04d%04d", 2021, index); // Sai prefix (ST thay vì SV)
            case 2: return String.format("SV%04d-%04d", 2021, index); // Có ký tự đặc biệt
            case 3: return String.format("sv%04d%04d", 2021, index); // Lowercase
            default: return String.format("SV%04d%05d", 2021, index); // Quá nhiều digits (9)
        }
    }

    /**
     * Generate invalid Email
     */
    private static String generateInvalidEmail(String fullName, int index) {
        String username = fullName.toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z]", "");
        int errorCase = random.nextInt(5);
        switch (errorCase) {
            case 0: return username + index + ".student.edu.vn"; // Thiếu @
            case 1: return username + index + "@"; // Thiếu domain
            case 2: return username + " " + index + "@student.edu.vn"; // Có khoảng trắng
            case 3: return username + index + "@student"; // Thiếu TLD
            default: return username + index + "@@student.edu.vn"; // Double @
        }
    }

    /**
     * Generate valid phone
     */
    private static String generateValidPhone() {
        String[] prefixes = {"090", "091", "092", "093", "094", "096", "097", "098", "099"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return prefix + String.format("%07d", random.nextInt(10000000));
    }

    /**
     * Generate invalid Phone
     */
    private static String generateInvalidPhone() {
        int errorCase = random.nextInt(4);
        switch (errorCase) {
            case 0: return "090" + String.format("%06d", random.nextInt(1000000)); // Thiếu 1 số (9 digits)
            case 1: return "090" + String.format("%07d", random.nextInt(10000000)) + "a"; // Có chữ
            case 2: return "080" + String.format("%07d", random.nextInt(10000000)); // Sai prefix
            default: return "+84-90-" + String.format("%07d", random.nextInt(10000000)); // Có ký tự đặc biệt
        }
    }

    /**
     * Generate invalid GPA
     */
    private static String generateInvalidGPA() {
        int errorCase = random.nextInt(4);
        switch (errorCase) {
            case 0: return String.format("%.2f", 4.0 + random.nextDouble() * 2.0); // > 4.0
            case 1: return String.format("%.2f", -random.nextDouble() * 2.0); // < 0
            case 2: return "three point five"; // Text
            default: return "N/A"; // Non-numeric
        }
    }

    /**
     * Generate invalid Date of Birth
     */
    private static String generateInvalidDOB() {
        int errorCase = random.nextInt(4);
        switch (errorCase) {
            case 0: // Format sai (DD/MM/YYYY thay vì YYYY-MM-DD)
                LocalDate date = LocalDate.of(
                    ThreadLocalRandom.current().nextInt(2000, 2007),
                    ThreadLocalRandom.current().nextInt(1, 13),
                    ThreadLocalRandom.current().nextInt(1, 29)
                );
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
            case 1: // Quá già (< 17 tuổi)
                return "1990-01-01";
                
            case 2: // Quá trẻ (> 30 tuổi)
                return "2010-01-01";
                
            default: // Invalid date
                return "2003-13-45";
        }
    }

    /**
     * Apply missing fields
     */
    private static void applyMissingFields(StudentRecord student) {
        int errorCase = random.nextInt(4);
        switch (errorCase) {
            case 0: student.fullName = ""; break;
            case 1: student.email = null; break;
            case 2: student.phone = ""; break;
            default: student.classCode = null; break;
        }
    }

    /**
     * Generate invalid Class Code
     */
    private static String generateInvalidClassCode() {
        int errorCase = random.nextInt(3);
        switch (errorCase) {
            case 0: return "SE1K1"; // Thiếu leading zeros
            case 1: return "Software01K01"; // Quá dài
            default: return "se01k01"; // Lowercase
        }
    }

    /**
     * Student Record DTO
     */
    static class StudentRecord {
        String studentId;
        String fullName;
        String dateOfBirth;
        String gender;
        String email;
        String phone;
        String address;
        String city;
        String province;
        String postalCode;
        String classCode;
        String major;
        String faculty;
        String academicYear;
        String enrollmentDate;
        String gpa;
        String totalCredits;
        String status;
    }
}