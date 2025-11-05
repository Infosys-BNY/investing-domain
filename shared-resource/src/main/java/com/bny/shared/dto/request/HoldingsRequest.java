package com.bny.shared.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingsRequest {
    
    @NotBlank(message = "Account ID is required")
    @Size(max = 50, message = "Account ID must not exceed 50 characters")
    private String accountId;
    
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;
    
    private boolean includeTaxLots = false;
}
