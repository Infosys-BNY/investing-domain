package com.bny.lfdapi.dto.response;

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
public class PortfolioSummaryResponse {
    private String accountId;
    private BigDecimal totalMarketValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal totalUnrealizedGainLossPercent;
    private BigDecimal portfolioBeta;
    private BigDecimal annualDividendYield;
    private Integer holdingsCount;
    private List<AssetAllocationDto> assetAllocation;
    private Integer resultCode;
    private String errorMessage;
}
