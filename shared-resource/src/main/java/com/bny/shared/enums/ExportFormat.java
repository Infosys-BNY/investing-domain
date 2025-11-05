package com.bny.shared.enums;

public enum ExportFormat {
    EXCEL("Excel"),
    CSV("CSV"),
    PDF("PDF");
    
    private final String displayName;
    
    ExportFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
