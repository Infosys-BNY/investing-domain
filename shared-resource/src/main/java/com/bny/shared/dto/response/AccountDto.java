package com.bny.shared.dto.response;

import com.bny.shared.enums.AccountType;
import com.bny.shared.enums.RiskProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    
    @NotBlank(message = "Account ID is required")
    @Size(max = 50, message = "Account ID must not exceed 50 characters")
    private String accountId;
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String accountNumber;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    private String clientId;
    
    @Size(max = 200, message = "Client name must not exceed 200 characters")
    private String clientName;
    
    private BigDecimal marketValue;
    private BigDecimal cashBalance;
    private BigDecimal ytdPerformance;
    
    private RiskProfile riskProfile;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdated;
}
