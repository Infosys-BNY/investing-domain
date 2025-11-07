package com.bny.investing.client;

import com.bny.investing.client.dto.LfdAdvisorClientsResponse;
import com.bny.investing.client.dto.LfdHoldingsResponse;
import com.bny.investing.client.dto.LfdPortfolioSummaryResponse;
import com.bny.investing.dto.AccountDto;
import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.HoldingDto;
import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.investing.exception.ResourceNotFoundException;
import com.bny.investing.model.RiskProfile;
import com.bny.shared.dto.request.HoldingsRequest;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.mock.enabled", havingValue = "false")
@RequiredArgsConstructor
public class RestLfdClientService implements LfdClientService {
    
    private final RestTemplate restTemplate;
    
    @Value("${lfd.api.base-url:http://localhost:8081}")
    private String lfdApiBaseUrl;
    
    @Value("${lfd.api.max-page-size:100}")
    private int maxPageSize;
    
    @Override
    public List<ClientDto> getAdvisorClients(String advisorId) {
        String url = lfdApiBaseUrl + "/internal/advisors/" + advisorId + "/clients";
        
        HttpHeaders headers = createHeaders(advisorId);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        log.debug("Calling LFD API: GET {} with headers: {}", url, headers.keySet());
        
        try {
            ResponseEntity<LfdAdvisorClientsResponse> response = restTemplate.exchange(
                url + "?pageOffset=0&pageSize=" + maxPageSize,
                HttpMethod.GET,
                entity,
                LfdAdvisorClientsResponse.class
            );
            
            if (response.getBody() != null && response.getBody().getClients() != null) {
                log.info("Received {} clients from LFD API for advisor: {}", 
                    response.getBody().getClients().size(), advisorId);
                return transformToBackendClientDtos(response.getBody().getClients());
            }
            return List.of();
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling LFD API for advisor clients: {} - {} - Response Body: {}", 
                e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            throw new ResourceNotFoundException("Failed to retrieve clients for advisor: " + advisorId + " - HTTP " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Unexpected error calling LFD API for advisor clients: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Failed to retrieve clients for advisor: " + advisorId + " - " + e.getMessage());
        }
    }
    
    @Override
    public ClientDto getClientById(String clientId) {
        throw new UnsupportedOperationException("getClientById not implemented in RestLfdClientService");
    }
    
    @Override
    public List<AccountDto> getClientAccounts(String clientId) {
        throw new UnsupportedOperationException("getClientAccounts not implemented in RestLfdClientService");
    }
    
    @Override
    public AccountDto getAccountInfo(String accountId) {
        String url = lfdApiBaseUrl + "/internal/accounts/" + accountId;
        
        HttpHeaders headers = createHeaders("advisor-id-placeholder");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        log.debug("Calling LFD API: GET {}", url);
        
        try {
            ResponseEntity<com.bny.shared.dto.response.AccountDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                com.bny.shared.dto.response.AccountDto.class
            );
            
            log.info("Received account info from LFD API for account: {}", accountId);
            
            return transformToBackendAccountDto(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Error calling LFD API for account info: {} - {}", 
                e.getStatusCode(), e.getMessage());
            throw new ResourceNotFoundException("Account not found: " + accountId);
        } catch (Exception e) {
            log.error("Unexpected error calling LFD API for account info", e);
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
    
    @Override
    public HoldingsResponseDto getAccountHoldings(String accountId, Pageable pageable) {
        String url = lfdApiBaseUrl + "/internal/accounts/" + accountId + "/holdings";
        
        HttpHeaders headers = createHeaders("advisor-id-placeholder");
        
        HoldingsRequest request = new HoldingsRequest();
        request.setAccountId(accountId);
        request.setPageOffset(pageable.getPageNumber());
        request.setPageSize(pageable.getPageSize());
        
        HttpEntity<HoldingsRequest> entity = new HttpEntity<>(request, headers);
        
        log.debug("Calling LFD API: POST {} with request: {}", url, request);
        
        try {
            ResponseEntity<LfdHoldingsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                LfdHoldingsResponse.class
            );
            
            log.info("Received {} holdings from LFD API for account: {}", 
                response.getBody() != null ? response.getBody().getHoldings().size() : 0, accountId);
            
            return transformToHoldingsResponseDto(response.getBody(), accountId, pageable);
        } catch (HttpClientErrorException e) {
            log.error("Error calling LFD API for holdings: {} - {}", 
                e.getStatusCode(), e.getMessage());
            throw new ResourceNotFoundException("Account not found: " + accountId);
        } catch (Exception e) {
            log.error("Unexpected error calling LFD API for holdings", e);
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
    
    @Override
    public PortfolioSummaryDto getPortfolioSummary(String accountId) {
        String url = lfdApiBaseUrl + "/internal/accounts/" + accountId + "/summary";
        
        HttpHeaders headers = createHeaders("advisor-id-placeholder");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        log.debug("Calling LFD API: GET {}", url);
        
        try {
            ResponseEntity<LfdPortfolioSummaryResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                LfdPortfolioSummaryResponse.class
            );
            
            log.info("Received portfolio summary from LFD API for account: {}", accountId);
            
            return transformToPortfolioSummaryDto(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Error calling LFD API for portfolio summary: {} - {}", 
                e.getStatusCode(), e.getMessage());
            throw new ResourceNotFoundException("Account not found: " + accountId);
        } catch (Exception e) {
            log.error("Unexpected error calling LFD API for portfolio summary", e);
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
    
    private HttpHeaders createHeaders(String advisorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-ID", "integration-test-user");
        headers.set("X-Advisor-ID", advisorId);
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("X-Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return headers;
    }
    
    private List<ClientDto> transformToBackendClientDtos(List<com.bny.shared.dto.response.ClientDto> sharedClients) {
        if (sharedClients == null) {
            return List.of();
        }
        
        return sharedClients.stream()
            .map(this::transformToBackendClientDto)
            .collect(Collectors.toList());
    }
    
    private ClientDto transformToBackendClientDto(com.bny.shared.dto.response.ClientDto sharedClient) {
        List<AccountDto> backendAccounts = new ArrayList<>();
        if (sharedClient.getAccounts() != null) {
            backendAccounts = sharedClient.getAccounts().stream()
                .map(this::transformToBackendAccountDto)
                .collect(Collectors.toList());
        }
        
        return ClientDto.builder()
            .clientId(sharedClient.getClientId())
            .clientName(sharedClient.getClientName())
            .advisorId(sharedClient.getAdvisorId())
            .advisorName(sharedClient.getAdvisorName())
            .totalMarketValue(sharedClient.getTotalMarketValue())
            .taxId(sharedClient.getTaxId())
            .accounts(backendAccounts)
            .build();
    }
    
    private AccountDto transformToBackendAccountDto(com.bny.shared.dto.response.AccountDto sharedAccount) {
        return AccountDto.builder()
            .accountId(sharedAccount.getAccountId())
            .accountNumber(sharedAccount.getAccountNumber())
            .accountType(parseAccountType(sharedAccount.getAccountType()))
            .clientId(sharedAccount.getClientId())
            .clientName(sharedAccount.getClientName())
            .marketValue(sharedAccount.getMarketValue())
            .cashBalance(sharedAccount.getCashBalance())
            .ytdPerformance(sharedAccount.getYtdPerformance())
            .lastActivity(sharedAccount.getLastUpdated())
            .build();
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
    
    private com.bny.investing.model.AssetClass parseAssetClass(String assetClassStr) {
        if (assetClassStr == null) {
            return null;
        }
        try {
            return com.bny.investing.model.AssetClass.valueOf(assetClassStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid asset class value: {}", assetClassStr);
            return null;
        }
    }
    
    private com.bny.investing.model.AccountType parseAccountType(com.bny.shared.enums.AccountType accountType) {
        if (accountType == null) {
            return null;
        }
        try {
            return com.bny.investing.model.AccountType.valueOf(accountType.name());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type value: {}", accountType);
            return null;
        }
    }
    
    private HoldingsResponseDto transformToHoldingsResponseDto(
            LfdHoldingsResponse lfdResponse, String accountId, Pageable pageable) {
        if (lfdResponse == null) {
            return HoldingsResponseDto.builder()
                .holdings(List.of())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(0)
                .totalPages(0)
                .build();
        }
        
        AccountDto accountInfo = getAccountInfo(accountId);
        PortfolioSummaryDto summary = getPortfolioSummary(accountId);
        
        List<HoldingDto> backendHoldings = transformToBackendHoldingDtos(lfdResponse.getHoldings());
        
        int totalElements = lfdResponse.getTotalCount() != null ? lfdResponse.getTotalCount() : 0;
        int totalPages = pageable.getPageSize() > 0 
            ? (int) Math.ceil((double) totalElements / pageable.getPageSize()) 
            : 0;
        
        return HoldingsResponseDto.builder()
            .accountInfo(accountInfo)
            .summary(summary)
            .holdings(backendHoldings)
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(totalElements)
            .totalPages(totalPages)
            .build();
    }
    
    private List<HoldingDto> transformToBackendHoldingDtos(
            List<com.bny.shared.dto.response.HoldingDto> sharedHoldings) {
        if (sharedHoldings == null) {
            return List.of();
        }
        
        return sharedHoldings.stream()
            .map(this::transformToBackendHoldingDto)
            .collect(Collectors.toList());
    }
    
    private HoldingDto transformToBackendHoldingDto(com.bny.shared.dto.response.HoldingDto sharedHolding) {
        return HoldingDto.builder()
            .symbol(sharedHolding.getSymbol())
            .securityName(sharedHolding.getSecurityName())
            .quantity(sharedHolding.getQuantity())
            .currentPrice(sharedHolding.getCurrentPrice())
            .priceChange(sharedHolding.getPriceChange())
            .priceChangePercent(sharedHolding.getPriceChangePercent())
            .costBasis(sharedHolding.getCostBasis())
            .totalCost(sharedHolding.getTotalCost())
            .marketValue(sharedHolding.getMarketValue())
            .unrealizedGainLoss(sharedHolding.getUnrealizedGainLoss())
            .unrealizedGainLossPercent(sharedHolding.getUnrealizedGainLossPercent())
            .portfolioPercent(sharedHolding.getPortfolioPercent())
            .sector(sharedHolding.getSector())
            .assetClass(parseAssetClass(sharedHolding.getAssetClass()))
            .hasAlerts(sharedHolding.getHasAlerts() != null ? sharedHolding.getHasAlerts() : false)
            .taxLotCount(sharedHolding.getTaxLotCount() != null ? sharedHolding.getTaxLotCount() : 1)
            .build();
    }
    
    private PortfolioSummaryDto transformToPortfolioSummaryDto(LfdPortfolioSummaryResponse lfdResponse) {
        if (lfdResponse == null) {
            return PortfolioSummaryDto.builder().build();
        }
        
        return PortfolioSummaryDto.builder()
            .totalMarketValue(lfdResponse.getTotalMarketValue())
            .totalCostBasis(lfdResponse.getTotalCostBasis())
            .totalUnrealizedGainLoss(lfdResponse.getTotalUnrealizedGainLoss())
            .numberOfHoldings(lfdResponse.getHoldingsCount())
            .portfolioBeta(lfdResponse.getPortfolioBeta())
            .dividendYield(lfdResponse.getAnnualDividendYield())
            .asOfDate(LocalDateTime.now())
            .build();
    }
}
