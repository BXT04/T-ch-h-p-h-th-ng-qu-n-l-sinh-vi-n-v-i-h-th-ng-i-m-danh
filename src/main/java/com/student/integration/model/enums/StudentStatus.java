package com.student.integration.model.enums;

/**
 * Enum cho trạng thái sinh viên
 */
public enum StudentStatus {
    ACTIVE("Active", "Đang học"),
    INACTIVE("Inactive", "Tạm ngưng"),
    SUSPENDED("Suspended", "Bị đình chỉ"),
    GRADUATED("Graduated", "Đã tốt nghiệp");
    
    private final String displayName;
    private final String vietnameseName;
    
    StudentStatus(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
    
    public static StudentStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (StudentStatus status : StudentStatus.values()) {
            if (status.displayName.equalsIgnoreCase(value) || 
                status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}