package com.student.integration.model.enums;

/**
 * Enum cho giới tính
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Parse từ string (case-insensitive)
     */
    public static Gender fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (Gender gender : Gender.values()) {
            if (gender.displayName.equalsIgnoreCase(value) || 
                gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        return null;
    }
}