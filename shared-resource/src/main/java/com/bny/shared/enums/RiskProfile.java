package com.bny.shared.enums;

public enum RiskProfile {
    CONSERVATIVE("Conservative"),
    MODERATE("Moderate"),
    AGGRESSIVE("Aggressive");
    
    private final String displayName;
    
    RiskProfile(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
