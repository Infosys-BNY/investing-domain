package com.bny.investing.service;

import com.bny.investing.client.LfdClientService;
import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.investing.dto.PortfolioSummaryDto;
import com.bny.investing.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingsServiceTest {

    @Mock
    private LfdClientService lfdClientService;

    @InjectMocks
    private HoldingsService holdingsService;

    private HoldingsResponseDto mockHoldingsResponse;
    private PortfolioSummaryDto mockPortfolioSummary;

    @BeforeEach
    void setUp() {
        mockPortfolioSummary = PortfolioSummaryDto.builder()
            .totalMarketValue(new BigDecimal("250000"))
            .totalCostBasis(new BigDecimal("220495"))
            .totalUnrealizedGainLoss(new BigDecimal("29505"))
            .totalRealizedGainLossYTD(new BigDecimal("5420.75"))
            .numberOfHoldings(4)
            .portfolioBeta(new BigDecimal("0.92"))
            .dividendYield(new BigDecimal("1.85"))
            .asOfDate(LocalDateTime.now())
            .build();

        mockHoldingsResponse = HoldingsResponseDto.builder()
            .summary(mockPortfolioSummary)
            .holdings(List.of())
            .page(0)
            .size(50)
            .totalElements(4)
            .totalPages(1)
            .build();
    }

    @Test
    void testGetAccountHoldings_Success() {
        String accountId = "ACC001";
        Pageable pageable = PageRequest.of(0, 50);
        
        when(lfdClientService.getAccountHoldings(accountId, pageable)).thenReturn(mockHoldingsResponse);
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(accountId, pageable);
        
        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertEquals(new BigDecimal("250000"), response.getSummary().getTotalMarketValue());
        assertEquals(4, response.getSummary().getNumberOfHoldings());
        assertEquals(0, response.getPage());
        assertEquals(50, response.getSize());
        verify(lfdClientService).getAccountHoldings(accountId, pageable);
    }

    @Test
    void testGetAccountHoldings_WithPagination() {
        String accountId = "ACC001";
        Pageable pageable = PageRequest.of(1, 10);
        
        when(lfdClientService.getAccountHoldings(accountId, pageable)).thenReturn(mockHoldingsResponse);
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(accountId, pageable);
        
        assertNotNull(response);
        verify(lfdClientService).getAccountHoldings(accountId, pageable);
    }

    @Test
    void testGetAccountHoldings_NotFound() {
        String accountId = "nonexistent";
        Pageable pageable = PageRequest.of(0, 50);
        
        when(lfdClientService.getAccountHoldings(accountId, pageable)).thenThrow(new RuntimeException("Not found"));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            holdingsService.getAccountHoldings(accountId, pageable);
        });
    }

    @Test
    void testGetPortfolioSummary_Success() {
        String accountId = "ACC001";
        
        when(lfdClientService.getPortfolioSummary(accountId)).thenReturn(mockPortfolioSummary);
        
        PortfolioSummaryDto response = holdingsService.getPortfolioSummary(accountId);
        
        assertNotNull(response);
        assertEquals(new BigDecimal("250000"), response.getTotalMarketValue());
        assertEquals(new BigDecimal("220495"), response.getTotalCostBasis());
        assertEquals(new BigDecimal("29505"), response.getTotalUnrealizedGainLoss());
        assertEquals(4, response.getNumberOfHoldings());
        assertNotNull(response.getAsOfDate());
        verify(lfdClientService).getPortfolioSummary(accountId);
    }

    @Test
    void testGetPortfolioSummary_NotFound() {
        String accountId = "nonexistent";
        
        when(lfdClientService.getPortfolioSummary(accountId)).thenThrow(new RuntimeException("Not found"));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            holdingsService.getPortfolioSummary(accountId);
        });
    }

    @Test
    void testGetPortfolioSummary_PositiveGains() {
        String accountId = "ACC001";
        
        when(lfdClientService.getPortfolioSummary(accountId)).thenReturn(mockPortfolioSummary);
        
        PortfolioSummaryDto response = holdingsService.getPortfolioSummary(accountId);
        
        assertNotNull(response);
        assertTrue(response.getTotalUnrealizedGainLoss().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalMarketValue().compareTo(response.getTotalCostBasis()) > 0);
    }
}
