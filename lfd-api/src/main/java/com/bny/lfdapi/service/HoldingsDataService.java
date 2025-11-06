package com.bny.lfdapi.service;

import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.shared.dto.response.HoldingDto;
import com.bny.shared.dto.common.StoredProcedureRequest;
import com.bny.shared.dto.common.StoredProcedureResponse;
import com.bny.lfdapi.dto.response.HoldingsResponse;
import com.bny.lfdapi.dto.response.PortfolioSummaryResponse;
import com.bny.lfdapi.dto.response.AssetAllocationDto;
import com.bny.shared.exception.DatabaseOperationException;
import com.bny.shared.service.StoredProcedureExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HoldingsDataService {

    @Autowired
    private StoredProcedureExecutor storedProcedureExecutor;
    
    @Autowired
    private ObjectMapper objectMapper;

    public HoldingsResponse getAccountHoldings(HoldingsRequest request) {
        log.debug("Getting holdings for account: {}", request.getAccountId());
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_account_id", request.getAccountId());
        parameters.put("p_as_of_date", request.getAsOfDate());
        parameters.put("p_asset_classes", convertToJson(request.getAssetClasses()));
        parameters.put("p_sort_field", request.getSortField());
        parameters.put("p_sort_direction", request.getSortDirection());
        parameters.put("p_page_offset", request.getPageOffset());
        parameters.put("p_page_size", request.getPageSize());
        
        StoredProcedureRequest spRequest = StoredProcedureRequest.builder()
            .procedureName("sp_get_account_holdings")
            .parameters(parameters)
            .build();
        
        StoredProcedureResponse spResponse = storedProcedureExecutor.execute(spRequest);
        
        List<HoldingDto> holdings = extractHoldingsFromResponse(spResponse);
        Integer totalCount = extractTotalCount(spResponse);
        
        return HoldingsResponse.builder()
            .holdings(holdings)
            .totalCount(totalCount)
            .pageOffset(request.getPageOffset())
            .pageSize(request.getPageSize())
            .resultCode(spResponse.getResultCode())
            .errorMessage(spResponse.getErrorMessage())
            .build();
    }

    public PortfolioSummaryResponse getPortfolioSummary(String accountId) {
        log.debug("Getting portfolio summary for account: {}", accountId);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_account_id", accountId);
        
        StoredProcedureRequest spRequest = StoredProcedureRequest.builder()
            .procedureName("sp_get_portfolio_summary")
            .parameters(parameters)
            .build();
        
        StoredProcedureResponse spResponse = storedProcedureExecutor.execute(spRequest);
        
        return extractPortfolioSummaryFromResponse(accountId, spResponse);
    }

    private List<HoldingDto> extractHoldingsFromResponse(StoredProcedureResponse response) {
        if (response.getData() == null) {
            return new ArrayList<>();
        }
        
        try {
            if (response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) response.getData();
                
                List<HoldingDto> holdings = new ArrayList<>();
                for (Map<String, Object> row : resultList) {
                    HoldingDto holding = mapRowToHoldingDto(row);
                    holdings.add(holding);
                }
                return holdings;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error extracting holdings from stored procedure response", e);
            throw new DatabaseOperationException("Failed to parse holdings data", e);
        }
    }

    private HoldingDto mapRowToHoldingDto(Map<String, Object> row) {
        return HoldingDto.builder()
            .accountId((String) row.get("account_id"))
            .symbol((String) row.get("symbol"))
            .securityName((String) row.get("security_name"))
            .assetClass((String) row.get("asset_class"))
            .quantity((BigDecimal) row.get("quantity"))
            .costBasis((BigDecimal) row.get("cost_basis"))
            .currentPrice((BigDecimal) row.get("current_price"))
            .marketValue((BigDecimal) row.get("market_value"))
            .unrealizedGainLoss((BigDecimal) row.get("unrealized_gain_loss"))
            .unrealizedGainLossPercent((BigDecimal) row.get("unrealized_gain_loss_percent"))
            .purchaseDate(convertToLocalDate(row.get("purchase_date")))
            .priceDate(convertToLocalDate(row.get("price_date")))
            .build();
    }

    private PortfolioSummaryResponse extractPortfolioSummaryFromResponse(
            String accountId, StoredProcedureResponse response) {
        
        Map<String, Object> outputParams = response.getOutputParameters();
        if (outputParams == null) {
            return PortfolioSummaryResponse.builder()
                .accountId(accountId)
                .resultCode(response.getResultCode())
                .errorMessage(response.getErrorMessage())
                .build();
        }
        
        List<AssetAllocationDto> assetAllocation = extractAssetAllocation(response.getData());
        
        return PortfolioSummaryResponse.builder()
            .accountId(accountId)
            .totalMarketValue((BigDecimal) outputParams.get("p_total_market_value"))
            .totalCostBasis((BigDecimal) outputParams.get("p_total_cost_basis"))
            .totalUnrealizedGainLoss((BigDecimal) outputParams.get("p_total_unrealized_gain_loss"))
            .totalUnrealizedGainLossPercent((BigDecimal) outputParams.get("p_unrealized_gain_loss_percent"))
            .portfolioBeta((BigDecimal) outputParams.get("p_portfolio_beta"))
            .annualDividendYield((BigDecimal) outputParams.get("p_annual_dividend_yield"))
            .holdingsCount((Integer) outputParams.get("p_holdings_count"))
            .assetAllocation(assetAllocation)
            .resultCode(response.getResultCode())
            .errorMessage(response.getErrorMessage())
            .build();
    }

    private List<AssetAllocationDto> extractAssetAllocation(Object data) {
        if (data == null) {
            return new ArrayList<>();
        }
        
        try {
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) data;
                
                List<AssetAllocationDto> allocations = new ArrayList<>();
                for (Map<String, Object> row : resultList) {
                    AssetAllocationDto allocation = AssetAllocationDto.builder()
                        .assetClass((String) row.get("asset_class"))
                        .marketValue((BigDecimal) row.get("market_value"))
                        .percentage((BigDecimal) row.get("percentage"))
                        .holdingsCount((Integer) row.get("holdings_count"))
                        .build();
                    allocations.add(allocation);
                }
                return allocations;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error extracting asset allocation from stored procedure response", e);
            return new ArrayList<>();
        }
    }

    private Integer extractTotalCount(StoredProcedureResponse response) {
        if (response.getOutputParameters() != null) {
            Object totalCount = response.getOutputParameters().get("p_total_count");
            if (totalCount instanceof Integer) {
                return (Integer) totalCount;
            }
        }
        return 0;
    }

    private String convertToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Failed to convert list to JSON", e);
            return null;
        }
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }
}
