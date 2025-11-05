package com.bny.lfdapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingsRequest {
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    private LocalDate asOfDate;
    
    private List<String> assetClasses;
    
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
