package com.student.integration.model.enums;

/**
 * Enum cho các loại lỗi validation
 */
public enum ErrorType {
    INVALID_FORMAT("Invalid Format", "Format không hợp lệ"),
    MISSING_FIELD("Missing Required Field", "Thiếu trường bắt buộc"),
    OUT_OF_RANGE("Value Out of Range", "Giá trị ngoài phạm vi"),
    DUPLICATE("Duplicate Value", "Giá trị trùng lặp"),
    INVALID_REFERENCE("Invalid Reference", "Tham chiếu không hợp lệ"),
    DATA_INCONSISTENCY("Data Inconsistency", "Dữ liệu không nhất quán"),
    BUSINESS_RULE_VIOLATION("Business Rule Violation", "Vi phạm quy tắc nghiệp vụ");
    
    private final String displayName;
    private final String vietnameseName;
    
    ErrorType(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
}