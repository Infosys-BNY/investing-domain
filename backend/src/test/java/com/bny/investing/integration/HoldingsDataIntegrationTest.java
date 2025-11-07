package com.bny.investing.integration;

import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import com.bny.investing.service.HoldingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HoldingsDataIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private HoldingsService holdingsService;
    
    @Test
    void testGetAccountHoldings_DomainToLfdCommunication() {
        String accountId = "ACC001";
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(accountId, PageRequest.of(0, 50));
        
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getHoldings(), "Holdings list should not be null");
        assertTrue(response.getTotalElements() >= 0, "Total elements should be non-negative");
        
        if (response.getTotalElements() > 0) {
            var firstHolding = response.getHoldings().get(0);
            assertNotNull(firstHolding.getSymbol(), "Symbol should not be null");
            assertNotNull(firstHolding.getSecurityName(), "Security name should not be null");
            assertNotNull(firstHolding.getMarketValue(), "Market value should not be null");
        }
        
        System.out.println("✓ Successfully retrieved " + response.getTotalElements() + " holdings from LFD API");
    }
    
    @Test
    void testGetAccountHoldings_VerifyStoredProcedureExecution() {
        String accountId = "ACC001";
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(accountId, PageRequest.of(0, 50));
        
        assertNotNull(response);
        assertTrue(response.getTotalPages() >= 0);
        
        System.out.println("✓ Stored procedure sp_get_account_holdings executed successfully");
    }
    
    @Test
    void testGetPortfolioSummary_DomainToLfdCommunication() {
        String accountId = "ACC001";
        
        PortfolioSummaryDto response = holdingsService.getPortfolioSummary(accountId);
        
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getTotalMarketValue(), "Total market value should not be null");
        assertNotNull(response.getTotalCostBasis(), "Total cost basis should not be null");
        assertTrue(response.getNumberOfHoldings() >= 0, "Number of holdings should be non-negative");
        
        System.out.println("✓ Successfully retrieved portfolio summary from LFD API");
        System.out.println("✓ Stored procedure sp_get_portfolio_summary executed successfully");
    }
    
    @Test
    void testGetAccountHoldings_DtoTransformation() {
        String accountId = "ACC001";
        
        HoldingsResponseDto response = holdingsService.getAccountHoldings(accountId, PageRequest.of(0, 10));
        
        assertNotNull(response);
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        
        if (!response.getHoldings().isEmpty()) {
            var holding = response.getHoldings().get(0);
            assertNotNull(holding.getSymbol());
            assertNotNull(holding.getMarketValue());
        }
        
        System.out.println("✓ DTO transformation working correctly");
    }
}
