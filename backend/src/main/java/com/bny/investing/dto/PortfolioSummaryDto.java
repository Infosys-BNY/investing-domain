package com.bny.investing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryDto {
    private BigDecimal totalMarketValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal totalRealizedGainLossYTD;
    private int numberOfHoldings;
    private BigDecimal portfolioBeta;
    private BigDecimal dividendYield;
    private LocalDateTime asOfDate;
}
