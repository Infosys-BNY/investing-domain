package com.bny.lfdapi.controller;

import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.shared.dto.response.HoldingDto;
import com.bny.lfdapi.dto.response.HoldingsResponse;
import com.bny.lfdapi.dto.response.PortfolioSummaryResponse;
import com.bny.lfdapi.service.HoldingsDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalHoldingsControllerTest {

    @Mock
    private HoldingsDataService holdingsDataService;

    @InjectMocks
    private InternalHoldingsController controller;

    private HoldingsRequest holdingsRequest;
    private HoldingsResponse holdingsResponse;
    private PortfolioSummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        holdingsRequest = HoldingsRequest.builder()
            .accountId("account123")
            .pageOffset(0)
            .pageSize(50)
            .build();

        List<HoldingDto> holdings = Arrays.asList(
            HoldingDto.builder()
                .accountId("account123")
                .symbol("AAPL")
                .securityName("Apple Inc.")
                .assetClass("Equity")
                .quantity(new BigDecimal("100"))
                .currentPrice(new BigDecimal("150.00"))
                .marketValue(new BigDecimal("15000.00"))
                .build(),
            HoldingDto.builder()
                .accountId("account123")
                .symbol("MSFT")
                .securityName("Microsoft Corp.")
                .assetClass("Equity")
                .quantity(new BigDecimal("50"))
                .currentPrice(new BigDecimal("300.00"))
                .marketValue(new BigDecimal("15000.00"))
                .build()
        );

        holdingsResponse = HoldingsResponse.builder()
            .holdings(holdings)
            .totalCount(2)
            .pageOffset(0)
            .pageSize(50)
            .resultCode(0)
            .build();

        summaryResponse = PortfolioSummaryResponse.builder()
            .accountId("account123")
            .totalMarketValue(new BigDecimal("30000.00"))
            .totalCostBasis(new BigDecimal("25000.00"))
            .totalUnrealizedGainLoss(new BigDecimal("5000.00"))
            .holdingsCount(2)
            .resultCode(0)
            .build();
    }

    @Test
    void getAccountHoldings_Success() {
        when(holdingsDataService.getAccountHoldings(any(HoldingsRequest.class)))
            .thenReturn(holdingsResponse);

        ResponseEntity<HoldingsResponse> response = 
            controller.getAccountHoldings("account123", holdingsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHoldings()).hasSize(2);
        assertThat(response.getBody().getTotalCount()).isEqualTo(2);
        assertThat(response.getBody().getResultCode()).isEqualTo(0);
        
        verify(holdingsDataService).getAccountHoldings(any(HoldingsRequest.class));
    }

    @Test
    void getPortfolioSummary_Success() {
        when(holdingsDataService.getPortfolioSummary(eq("account123")))
            .thenReturn(summaryResponse);

        ResponseEntity<PortfolioSummaryResponse> response = 
            controller.getPortfolioSummary("account123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountId()).isEqualTo("account123");
        assertThat(response.getBody().getTotalMarketValue())
            .isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.getBody().getHoldingsCount()).isEqualTo(2);
        
        verify(holdingsDataService).getPortfolioSummary("account123");
    }

    @Test
    void getAccountHoldings_NonZeroResultCode() {
        HoldingsResponse errorResponse = HoldingsResponse.builder()
            .holdings(Arrays.asList())
            .totalCount(0)
            .pageOffset(0)
            .pageSize(50)
            .resultCode(1)
            .errorMessage("Database error")
            .build();

        when(holdingsDataService.getAccountHoldings(any(HoldingsRequest.class)))
            .thenReturn(errorResponse);

        ResponseEntity<HoldingsResponse> response = 
            controller.getAccountHoldings("account123", holdingsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResultCode()).isEqualTo(1);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Database error");
    }
}
