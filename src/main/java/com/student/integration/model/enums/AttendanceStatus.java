package com.student.integration.model.enums;

/**
 * Enum cho trạng thái điểm danh
 */
public enum AttendanceStatus {
    PRESENT("Present", "Có mặt"),
    ABSENT("Absent", "Vắng mặt"),
    LATE("Late", "Đi muộn"),
    EXCUSED("Excused", "Vắng có phép");
    
    private final String displayName;
    private final String vietnameseName;
    
    AttendanceStatus(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
    
    public static AttendanceStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status.displayName.equalsIgnoreCase(value) || 
                status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}