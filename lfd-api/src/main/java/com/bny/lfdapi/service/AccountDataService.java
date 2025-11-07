package com.bny.lfdapi.service;

import com.bny.shared.dto.response.AccountDto;
import com.bny.shared.enums.AccountType;
import com.bny.shared.enums.RiskProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class AccountDataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public AccountDto getAccountById(String accountId) {
        log.debug("Getting account details for account: {}", accountId);
        
        String sql = """
            SELECT a.account_id, a.account_number, a.account_type, a.client_id, 
                   c.client_name, a.market_value, a.cash_balance, a.ytd_performance,
                   a.risk_profile, a.last_updated
            FROM accounts a
            JOIN clients c ON a.client_id = c.client_id
            WHERE a.account_id = ?
        """;
        
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, accountId);
            return mapRowToAccountDto(row);
        } catch (Exception e) {
            log.error("Error fetching account details for account: {}", accountId, e);
            throw new RuntimeException("Account not found: " + accountId, e);
        }
    }
    
    private AccountDto mapRowToAccountDto(Map<String, Object> row) {
        return AccountDto.builder()
            .accountId((String) row.get("account_id"))
            .accountNumber((String) row.get("account_number"))
            .accountType(parseAccountType((String) row.get("account_type")))
            .clientId((String) row.get("client_id"))
            .clientName((String) row.get("client_name"))
            .marketValue((BigDecimal) row.get("market_value"))
            .cashBalance((BigDecimal) row.get("cash_balance"))
            .ytdPerformance((BigDecimal) row.get("ytd_performance"))
            .riskProfile(parseRiskProfile((String) row.get("risk_profile")))
            .lastUpdated(convertToLocalDateTime(row.get("last_updated")))
            .build();
    }
    
    private AccountType parseAccountType(String accountTypeStr) {
        if (accountTypeStr == null) {
            return null;
        }
        try {
            return AccountType.valueOf(accountTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type value: {}", accountTypeStr);
            return null;
        }
    }
    
    private RiskProfile parseRiskProfile(String riskProfileStr) {
        if (riskProfileStr == null) {
            return null;
        }
        try {
            return RiskProfile.valueOf(riskProfileStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid risk profile value: {}", riskProfileStr);
            return null;
        }
    }
    
    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }
}
