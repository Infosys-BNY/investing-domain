package com.bny.investing.dto;

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
public class PortfolioSummaryDto {
    private BigDecimal totalMarketValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private BigDecimal ytdPerformance;
    private int numberOfAccounts;
    private int numberOfHoldings;
    private BigDecimal cashBalance;
    private List<AssetAllocationDto> assetAllocation;
}
