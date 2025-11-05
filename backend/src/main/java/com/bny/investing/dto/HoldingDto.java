package com.bny.investing.dto;

import com.bny.investing.model.AssetClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDto {
    private String symbol;
    private String securityName;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private BigDecimal costBasis;
    private BigDecimal totalCost;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private BigDecimal portfolioPercent;
    private String sector;
    private AssetClass assetClass;
    private boolean hasAlerts;
    private int taxLotCount;
}
