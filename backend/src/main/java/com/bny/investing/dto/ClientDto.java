package com.bny.investing.dto;

import com.bny.investing.model.RiskProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private String clientId;
    private String clientName;
    private List<AccountDto> accounts;
    private String taxId;
    private RiskProfile riskProfile;
    private BigDecimal totalMarketValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal ytdPerformance;
    private LocalDateTime lastAccessed;
    private String advisorId;
    private String advisorName;
}
