package com.bny.shared.enums;

public enum ExportStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    ExportStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
