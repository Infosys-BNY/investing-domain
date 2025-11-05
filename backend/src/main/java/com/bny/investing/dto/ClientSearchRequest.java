package com.bny.investing.dto;

import com.bny.investing.model.AccountType;
import com.bny.investing.model.ActivityStatus;
import com.bny.investing.model.PerformanceFilter;
import com.bny.investing.model.RiskProfile;
import com.bny.investing.model.SortDirection;
import com.bny.investing.model.SortField;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSearchRequest {
    @NotBlank(message = "Advisor ID is required")
    private String advisorId;
    
    private String clientName;
    private String accountNumber;
    private String taxId;
    private List<AccountType> accountTypes;
    private List<RiskProfile> riskProfiles;
    private PerformanceFilter performanceFilter;
    private ActivityStatus activityStatus;
    
    @Min(value = 0, message = "Page must be 0 or greater")
    private int page = 0;
    
    @Min(value = 1, message = "Size must be at least 1")
    private int size = 50;
    
    private SortField sortBy = SortField.CLIENT_NAME;
    private SortDirection sortDirection = SortDirection.ASC;
}
