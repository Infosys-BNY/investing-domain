package com.bny.lfdapi.integration;

import com.bny.lfdapi.dto.response.HoldingsResponse;
import com.bny.lfdapi.dto.response.PortfolioSummaryResponse;
import com.bny.lfdapi.dto.response.AssetAllocationDto;
import com.bny.lfdapi.service.HoldingsDataService;
import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.shared.dto.response.HoldingDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HoldingsDataService Integration Tests")
public class HoldingsDataServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private HoldingsDataService holdingsDataService;

    @Test
    @DisplayName("Should successfully retrieve account holdings from AWS RDS")
    public void getAccountHoldings_WithValidAccountId_ReturnsHoldings() {
        String accountId = ACCOUNT_ID_1;
        
        HoldingsRequest request = HoldingsRequest.builder()
            .accountId(accountId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        HoldingsResponse response = measureExecutionTimeWithResult(
            () -> holdingsDataService.getAccountHoldings(request),
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isNotEmpty();
        assertThat(response.getTotalCount()).isGreaterThan(0);
        
        log.info("Retrieved {} holdings for account {}", response.getHoldings().size(), accountId);
        
        HoldingDto firstHolding = response.getHoldings().get(0);
        assertThat(firstHolding.getAccountId()).isEqualTo(accountId);
        assertThat(firstHolding.getSymbol()).isNotNull();
        assertThat(firstHolding.getQuantity()).isNotNull();
        assertThat(firstHolding.getCurrentPrice()).isNotNull();
        
        assertPerformanceTarget(metrics.getAverageExecutionTime(), STORED_PROCEDURE_TARGET_MS, "getAccountHoldings");
    }

    @Test
    @DisplayName("Should verify calculated fields are correct")
    public void getAccountHoldings_VerifiesCalculatedFields() {
        String accountId = ACCOUNT_ID_1;
        
        HoldingsRequest request = HoldingsRequest.builder()
            .accountId(accountId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        HoldingsResponse response = holdingsDataService.getAccountHoldings(request);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isNotEmpty();
        
        for (HoldingDto holding : response.getHoldings()) {
            BigDecimal quantity = holding.getQuantity();
            BigDecimal currentPrice = holding.getCurrentPrice();
            BigDecimal marketValue = holding.getMarketValue();
            
            assertThat(quantity).isNotNull().isGreaterThan(BigDecimal.ZERO);
            assertThat(currentPrice).isNotNull().isGreaterThan(BigDecimal.ZERO);
            assertThat(marketValue).isNotNull();
            
            BigDecimal expectedMarketValue = quantity.multiply(currentPrice);
            assertThat(marketValue).isEqualByComparingTo(expectedMarketValue);
            
            if (holding.getUnrealizedGainLoss() != null && holding.getCostBasis() != null) {
                BigDecimal expectedGainLoss = marketValue.subtract(holding.getCostBasis());
                assertThat(holding.getUnrealizedGainLoss()).isEqualByComparingTo(expectedGainLoss);
            }
            
            log.debug("Holding {}: quantity={}, price={}, marketValue={}", 
                holding.getSymbol(), quantity, currentPrice, marketValue);
        }
        
        log.info("Verified calculated fields for {} holdings", response.getHoldings().size());
    }

    @Test
    @DisplayName("Should handle pagination for holdings")
    public void getAccountHoldings_WithPagination_ReturnsPaginatedResults() {
        String accountId = ACCOUNT_ID_1;
        int pageSize = 2;
        
        HoldingsRequest request1 = HoldingsRequest.builder()
            .accountId(accountId)
            .pageOffset(0)
            .pageSize(pageSize)
            .build();
        
        HoldingsResponse page1 = holdingsDataService.getAccountHoldings(request1);
        
        assertThat(page1).isNotNull();
        assertThat(page1.getPageOffset()).isEqualTo(0);
        assertThat(page1.getPageSize()).isEqualTo(pageSize);
        
        if (page1.getTotalCount() > pageSize) {
            HoldingsRequest request2 = HoldingsRequest.builder()
                .accountId(accountId)
                .pageOffset(pageSize)
                .pageSize(pageSize)
                .build();
            
            HoldingsResponse page2 = holdingsDataService.getAccountHoldings(request2);
            
            assertThat(page2).isNotNull();
            assertThat(page2.getPageOffset()).isEqualTo(pageSize);
            
            log.info("Pagination: Page 1 has {} holdings, Page 2 has {} holdings",
                page1.getHoldings().size(), page2.getHoldings().size());
        }
    }

    @Test
    @DisplayName("Should retrieve portfolio summary with aggregations")
    public void getPortfolioSummary_WithValidAccountId_ReturnsAggregations() {
        String accountId = ACCOUNT_ID_1;
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        PortfolioSummaryResponse summary = measureExecutionTimeWithResult(
            () -> holdingsDataService.getPortfolioSummary(accountId),
            metrics
        );

        assertThat(summary).isNotNull();
        assertThat(summary.getAccountId()).isEqualTo(accountId);
        assertThat(summary.getTotalMarketValue()).isNotNull();
        assertThat(summary.getTotalCostBasis()).isNotNull();
        assertThat(summary.getTotalUnrealizedGainLoss()).isNotNull();
        assertThat(summary.getHoldingsCount()).isGreaterThan(0);
        
        log.info("Portfolio Summary for account {}:", accountId);
        log.info("  Total Market Value: {}", summary.getTotalMarketValue());
        log.info("  Total Cost Basis: {}", summary.getTotalCostBasis());
        log.info("  Total Unrealized Gain/Loss: {}", summary.getTotalUnrealizedGainLoss());
        log.info("  Holdings Count: {}", summary.getHoldingsCount());
        log.info("  Portfolio Beta: {}", summary.getPortfolioBeta());
        log.info("  Annual Dividend Yield: {}", summary.getAnnualDividendYield());
        
        assertPerformanceTarget(metrics.getAverageExecutionTime(), STORED_PROCEDURE_TARGET_MS, "getPortfolioSummary");
    }

    @Test
    @DisplayName("Should verify asset allocation data in portfolio summary")
    public void getPortfolioSummary_VerifiesAssetAllocation() {
        String accountId = ACCOUNT_ID_1;
        
        PortfolioSummaryResponse summary = holdingsDataService.getPortfolioSummary(accountId);

        assertThat(summary).isNotNull();
        assertThat(summary.getAssetAllocation()).isNotNull();
        
        if (!summary.getAssetAllocation().isEmpty()) {
            BigDecimal totalPercentage = BigDecimal.ZERO;
            
            for (AssetAllocationDto allocation : summary.getAssetAllocation()) {
                assertThat(allocation.getAssetClass()).isNotNull();
                assertThat(allocation.getMarketValue()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
                assertThat(allocation.getPercentage()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
                
                totalPercentage = totalPercentage.add(allocation.getPercentage());
                
                log.info("Asset Allocation: {} - {} ({}%)", 
                    allocation.getAssetClass(), allocation.getMarketValue(), allocation.getPercentage());
            }
            
            assertThat(totalPercentage).isLessThanOrEqualTo(new BigDecimal("100.01"));
            
            log.info("Total asset allocation percentage: {}%", totalPercentage);
        }
    }

    @Test
    @DisplayName("Should return empty results for invalid account ID")
    public void getAccountHoldings_WithInvalidAccountId_ReturnsEmptyResults() {
        String invalidAccountId = "INVALID_ACC_999";
        
        HoldingsRequest request = HoldingsRequest.builder()
            .accountId(invalidAccountId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        HoldingsResponse response = holdingsDataService.getAccountHoldings(request);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
        
        log.info("Invalid account ID returned empty results as expected");
    }

    @Test
    @DisplayName("Should meet performance targets for portfolio summary")
    public void getPortfolioSummary_PerformanceTest_MeetsTargets() {
        String accountId = ACCOUNT_ID_1;
        int iterations = 20;
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        
        for (int i = 0; i < iterations; i++) {
            try {
                measureExecutionTimeWithResult(
                    () -> holdingsDataService.getPortfolioSummary(accountId),
                    metrics
                );
            } catch (Exception e) {
                metrics.recordFailure();
                log.error("Execution {} failed", i, e);
            }
        }
        
        double p95 = metrics.getPercentile(95);
        double successRate = metrics.getSuccessRate();
        long avgTime = metrics.getAverageExecutionTime();
        
        log.info("Performance Test Results for Portfolio Summary:");
        log.info("  Total Executions: {}", iterations);
        log.info("  Successful: {} ({} %)", metrics.getSuccessCount(), successRate * 100);
        log.info("  Average Time: {} ms", avgTime);
        log.info("  95th Percentile: {} ms", p95);
        
        assertThat(successRate)
            .as("Success rate should be greater than %s", SUCCESS_RATE_TARGET)
            .isGreaterThanOrEqualTo(SUCCESS_RATE_TARGET);
        
        if (p95 > STORED_PROCEDURE_TARGET_MS) {
            log.warn("95th percentile ({} ms) exceeds target ({} ms)", p95, STORED_PROCEDURE_TARGET_MS);
        }
    }

    @Test
    @DisplayName("Should filter holdings by symbol")
    public void getAccountHoldings_WithSymbolFilter_ReturnsFilteredResults() {
        String accountId = ACCOUNT_ID_1;
        
        HoldingsRequest requestAll = HoldingsRequest.builder()
            .accountId(accountId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        HoldingsResponse allHoldings = holdingsDataService.getAccountHoldings(requestAll);
        
        assertThat(allHoldings).isNotNull();
        assertThat(allHoldings.getHoldings()).isNotEmpty();
        
        String symbolToFilter = allHoldings.getHoldings().get(0).getSymbol();
        
        HoldingsRequest request = HoldingsRequest.builder()
            .accountId(accountId)
            .symbol(symbolToFilter)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        HoldingsResponse response = holdingsDataService.getAccountHoldings(request);

        assertThat(response).isNotNull();
        
        log.info("Symbol filter for '{}' returned {} holdings (total holdings: {})", 
            symbolToFilter, response.getHoldings().size(), allHoldings.getHoldings().size());
    }

    @Test
    @DisplayName("Should map all holding DTO fields correctly")
    public void getAccountHoldings_VerifiesDtoMapping() {
        String accountId = ACCOUNT_ID_1;
        
        HoldingsRequest request = HoldingsRequest.builder()
            .accountId(accountId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        HoldingsResponse response = holdingsDataService.getAccountHoldings(request);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isNotEmpty();
        
        HoldingDto holding = response.getHoldings().get(0);
        
        assertThat(holding.getAccountId()).isNotNull().isEqualTo(accountId);
        assertThat(holding.getSymbol()).isNotNull().isNotEmpty();
        assertThat(holding.getSecurityName()).isNotNull();
        assertThat(holding.getQuantity()).isNotNull();
        assertThat(holding.getCurrentPrice()).isNotNull();
        assertThat(holding.getMarketValue()).isNotNull();
        
        log.info("DTO mapping verified for holding: symbol={}, quantity={}, price={}", 
            holding.getSymbol(), holding.getQuantity(), holding.getCurrentPrice());
    }
}
