package com.bny.shared.dto.response;

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
    private BigDecimal totalUnrealizedGainLossPercent;
    private BigDecimal totalRealizedGainLossYTD;
    private Integer numberOfHoldings;
    private BigDecimal portfolioBeta;
    private BigDecimal dividendYield;
    private LocalDateTime asOfDate;
}
