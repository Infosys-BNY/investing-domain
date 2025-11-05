package com.bny.shared.util.mapping;

import com.bny.shared.enums.AssetClass;
import com.bny.shared.enums.AccountType;
import com.bny.shared.enums.RiskProfile;

public class EnumMapper {
    
    public static AssetClass toSharedAssetClass(Object backendAssetClass) {
        if (backendAssetClass == null) {
            return null;
        }
        
        String name = backendAssetClass.toString();
        
        try {
            return AssetClass.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static String toBackendAssetClassString(AssetClass sharedAssetClass) {
        if (sharedAssetClass == null) {
            return null;
        }
        
        if (sharedAssetClass == AssetClass.DERIVATIVE) {
            return "ALTERNATIVE";
        }
        
        return sharedAssetClass.name();
    }
    
    public static AccountType toSharedAccountType(Object backendAccountType) {
        if (backendAccountType == null) {
            return null;
        }
        
        String name = backendAccountType.toString();
        
        try {
            return AccountType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static String toBackendAccountTypeString(AccountType sharedAccountType) {
        if (sharedAccountType == null) {
            return null;
        }
        
        if (sharedAccountType == AccountType.UMA) {
            return null;
        }
        
        return sharedAccountType.name();
    }
    
    public static RiskProfile toSharedRiskProfile(Object backendRiskProfile) {
        if (backendRiskProfile == null) {
            return null;
        }
        
        String name = backendRiskProfile.toString();
        
        try {
            return RiskProfile.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static String toBackendRiskProfileString(RiskProfile sharedRiskProfile) {
        if (sharedRiskProfile == null) {
            return null;
        }
        
        return sharedRiskProfile.name();
    }
}
