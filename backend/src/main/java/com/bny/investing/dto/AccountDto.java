package com.bny.investing.dto;

import com.bny.investing.model.AccountType;
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
public class AccountDto {
    private String accountId;
    private String accountNumber;
    private AccountType accountType;
    private String accountName;
    private BigDecimal marketValue;
    private BigDecimal costBasis;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal ytdPerformance;
    private BigDecimal cashBalance;
    private LocalDate inceptionDate;
    private LocalDateTime lastActivity;
}
