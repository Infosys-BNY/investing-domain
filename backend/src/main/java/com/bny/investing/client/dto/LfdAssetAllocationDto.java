package com.bny.investing.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LfdAssetAllocationDto {
    private String assetClass;
    private BigDecimal marketValue;
    private BigDecimal percentage;
    private Integer holdingsCount;
}
