package com.bny.shared.enums;

public enum AssetClass {
    EQUITY("Equity"),
    FIXED_INCOME("Fixed Income"),
    ALTERNATIVE("Alternative"),
    CASH("Cash"),
    DERIVATIVE("Derivative");
    
    private final String displayName;
    
    AssetClass(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
