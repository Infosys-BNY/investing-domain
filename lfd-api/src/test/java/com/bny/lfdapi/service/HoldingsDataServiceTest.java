package com.bny.lfdapi.service;

import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.lfdapi.dto.response.HoldingsResponse;
import com.bny.lfdapi.dto.response.PortfolioSummaryResponse;
import com.bny.shared.dto.common.StoredProcedureRequest;
import com.bny.shared.dto.common.StoredProcedureResponse;
import com.bny.shared.service.StoredProcedureExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoldingsDataServiceTest {

    @Mock
    private StoredProcedureExecutor storedProcedureExecutor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HoldingsDataService holdingsDataService;

    private HoldingsRequest holdingsRequest;
    private List<Map<String, Object>> mockHoldingsData;
    private List<Map<String, Object>> mockAssetAllocationData;

    @BeforeEach
    void setUp() {
        holdingsRequest = HoldingsRequest.builder()
            .accountId("account123")
            .pageOffset(0)
            .pageSize(50)
            .build();

        mockHoldingsData = new ArrayList<>();
        Map<String, Object> holding1 = new HashMap<>();
        holding1.put("account_id", "account123");
        holding1.put("symbol", "AAPL");
        holding1.put("security_name", "Apple Inc.");
        holding1.put("asset_class", "Equity");
        holding1.put("quantity", new BigDecimal("100"));
        holding1.put("cost_basis", new BigDecimal("12000"));
        holding1.put("current_price", new BigDecimal("150.00"));
        holding1.put("market_value", new BigDecimal("15000.00"));
        holding1.put("unrealized_gain_loss", new BigDecimal("3000.00"));
        holding1.put("unrealized_gain_loss_percent", new BigDecimal("25.00"));
        mockHoldingsData.add(holding1);

        mockAssetAllocationData = new ArrayList<>();
        Map<String, Object> allocation1 = new HashMap<>();
        allocation1.put("asset_class", "Equity");
        allocation1.put("market_value", new BigDecimal("15000.00"));
        allocation1.put("percentage", new BigDecimal("100.00"));
        allocation1.put("holdings_count", 1);
        mockAssetAllocationData.add(allocation1);
    }

    @Test
    void getAccountHoldings_Success() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_count", 1);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(mockHoldingsData)
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        HoldingsResponse response = holdingsDataService.getAccountHoldings(holdingsRequest);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).hasSize(1);
        assertThat(response.getHoldings().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(response.getHoldings().get(0).getSecurityName()).isEqualTo("Apple Inc.");
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getResultCode()).isEqualTo(0);
        
        verify(storedProcedureExecutor).execute(any(StoredProcedureRequest.class));
    }

    @Test
    void getPortfolioSummary_Success() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_market_value", new BigDecimal("15000.00"));
        outputParams.put("p_total_cost_basis", new BigDecimal("12000.00"));
        outputParams.put("p_total_unrealized_gain_loss", new BigDecimal("3000.00"));
        outputParams.put("p_unrealized_gain_loss_percent", new BigDecimal("25.00"));
        outputParams.put("p_portfolio_beta", new BigDecimal("1.2"));
        outputParams.put("p_annual_dividend_yield", new BigDecimal("2.5"));
        outputParams.put("p_holdings_count", 1);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(mockAssetAllocationData)
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        PortfolioSummaryResponse response = 
            holdingsDataService.getPortfolioSummary("account123");

        assertThat(response).isNotNull();
        assertThat(response.getAccountId()).isEqualTo("account123");
        assertThat(response.getTotalMarketValue())
            .isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(response.getTotalCostBasis())
            .isEqualByComparingTo(new BigDecimal("12000.00"));
        assertThat(response.getHoldingsCount()).isEqualTo(1);
        assertThat(response.getAssetAllocation()).hasSize(1);
        assertThat(response.getResultCode()).isEqualTo(0);
        
        verify(storedProcedureExecutor).execute(any(StoredProcedureRequest.class));
    }

    @Test
    void getAccountHoldings_EmptyResults() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_count", 0);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(new ArrayList<>())
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        HoldingsResponse response = holdingsDataService.getAccountHoldings(holdingsRequest);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    void getAccountHoldings_NullData() {
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(null)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        HoldingsResponse response = holdingsDataService.getAccountHoldings(holdingsRequest);

        assertThat(response).isNotNull();
        assertThat(response.getHoldings()).isEmpty();
    }
}
