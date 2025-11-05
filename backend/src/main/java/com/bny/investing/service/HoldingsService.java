package com.bny.investing.service;

import com.bny.investing.client.LfdClientService;
import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.investing.dto.PortfolioSummaryDto;
import com.bny.investing.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HoldingsService {
    
    private final LfdClientService lfdClientService;
    
    @Cacheable(value = "holdings", key = "#accountId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public HoldingsResponseDto getAccountHoldings(String accountId, Pageable pageable) {
        try {
            return lfdClientService.getAccountHoldings(accountId, pageable);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
    
    @Cacheable(value = "portfolio-summary", key = "#accountId")
    @Transactional(readOnly = true)
    public PortfolioSummaryDto getPortfolioSummary(String accountId) {
        try {
            return lfdClientService.getPortfolioSummary(accountId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
}
