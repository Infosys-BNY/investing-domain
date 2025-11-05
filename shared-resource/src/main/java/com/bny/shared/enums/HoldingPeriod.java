package com.bny.shared.enums;

public enum HoldingPeriod {
    SHORT_TERM("Short-term"),
    LONG_TERM("Long-term");
    
    private final String displayName;
    
    HoldingPeriod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
