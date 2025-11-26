package com.student.integration.model.enums;

/**
 * Enum cho mức độ nghiêm trọng của lỗi
 */
public enum Severity {
    LOW(1, "Low", "Thấp"),
    MEDIUM(2, "Medium", "Trung bình"),
    HIGH(3, "High", "Cao"),
    CRITICAL(4, "Critical", "Nghiêm trọng");
    
    private final int level;
    private final String displayName;
    private final String vietnameseName;
    
    Severity(int level, String displayName, String vietnameseName) {
        this.level = level;
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getVietnameseName() {
        return vietnameseName;
    }
    
    public boolean isMoreSevereThan(Severity other) {
        return this.level > other.level;
    }
}