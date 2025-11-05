package com.bny.lfdapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDto {
    private String accountId;
    private String symbol;
    private String securityName;
    private String assetClass;
    private BigDecimal quantity;
    private BigDecimal costBasis;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private LocalDate purchaseDate;
    private LocalDate priceDate;
}
