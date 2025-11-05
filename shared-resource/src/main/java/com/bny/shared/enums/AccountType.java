package com.bny.shared.enums;

public enum AccountType {
    INDIVIDUAL("Individual"),
    JOINT("Joint"),
    IRA("IRA"),
    TRUST("Trust"),
    CORPORATE("Corporate"),
    UMA("Unified Managed Account");
    
    private final String displayName;
    
    AccountType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
