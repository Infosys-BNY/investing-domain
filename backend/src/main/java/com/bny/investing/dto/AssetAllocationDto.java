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
public class AssetAllocationDto {
    private AssetClass assetClass;
    private BigDecimal marketValue;
    private BigDecimal percentage;
}
