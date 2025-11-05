package com.bny.lfdapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSearchRequest {
    
    @NotBlank(message = "Advisor ID is required")
    private String advisorId;
    
    private String searchQuery;
    
    private List<String> accountTypes;
    
    private BigDecimal minMarketValue;
    
    private BigDecimal maxMarketValue;
    
    private String activityStatus;
    
    private String riskProfile;
    
    private String sortField;
    
    private String sortDirection;
    
    @Min(value = 0, message = "Page offset must be 0 or greater")
    @Builder.Default
    private Integer pageOffset = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer pageSize = 50;
}
