package com.bny.shared.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDto {
    
    @Size(max = 50, message = "Account ID must not exceed 50 characters")
    private String accountId;
    
    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;
    
    @Size(max = 200, message = "Security name must not exceed 200 characters")
    private String securityName;
    
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    
    private BigDecimal currentPrice;
    private BigDecimal costBasis;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private BigDecimal portfolioPercent;
    
    @Size(max = 100, message = "Sector must not exceed 100 characters")
    private String sector;
    
    @Size(max = 50, message = "Asset class must not exceed 50 characters")
    private String assetClass;
    
    private LocalDate purchaseDate;
    
    private LocalDate priceDate;
}
