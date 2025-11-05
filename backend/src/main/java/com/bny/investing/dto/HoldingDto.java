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
    private String cusip;
    private String securityName;
    private AssetClass assetClass;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal costBasis;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private BigDecimal ytdReturn;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
}
