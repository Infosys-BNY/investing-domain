package com.bny.lfdapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private String clientId;
    private String clientName;
    private String advisorId;
    private String advisorName;
    private Integer accountCount;
    private BigDecimal totalMarketValue;
    private String activityStatus;
    private String riskProfile;
    private LocalDate lastActivityDate;
    private LocalDateTime createdDate;
}
