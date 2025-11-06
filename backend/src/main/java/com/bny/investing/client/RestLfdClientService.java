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
    
    @Override
    public List<ClientDto> getAdvisorClients(String advisorId) {
        String url = lfdApiBaseUrl + "/internal/advisors/" + advisorId + "/clients";
        
        HttpHeaders headers = createHeaders(advisorId);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        log.debug("Calling LFD API: GET {} with headers: {}", url, headers.keySet());
        
        try {
            ResponseEntity<LfdAdvisorClientsResponse> response = restTemplate.exchange(
                url + "?pageOffset=0&pageSize=1000",
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
            log.error("Error calling LFD API for advisor clients: {} - {}", 
                e.getStatusCode(), e.getMessage());
            throw new ResourceNotFoundException("Failed to retrieve clients for advisor: " + advisorId);
        } catch (Exception e) {
            log.error("Unexpected error calling LFD API for advisor clients", e);
            throw new ResourceNotFoundException("Failed to retrieve clients for advisor: " + advisorId);
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
        return ClientDto.builder()
            .clientId(sharedClient.getClientId())
            .clientName(sharedClient.getClientName())
            .advisorId(sharedClient.getAdvisorId())
            .advisorName(sharedClient.getAdvisorName())
            .totalMarketValue(sharedClient.getTotalMarketValue())
            .riskProfile(parseRiskProfile(sharedClient.getRiskProfile()))
            .lastAccessed(sharedClient.getLastAccessed())
            .accounts(List.of())
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
        
        AccountDto accountInfo = AccountDto.builder()
            .accountId(accountId)
            .build();
        
        List<HoldingDto> backendHoldings = transformToBackendHoldingDtos(lfdResponse.getHoldings());
        
        int totalElements = lfdResponse.getTotalCount() != null ? lfdResponse.getTotalCount() : 0;
        int totalPages = pageable.getPageSize() > 0 
            ? (int) Math.ceil((double) totalElements / pageable.getPageSize()) 
            : 0;
        
        return HoldingsResponseDto.builder()
            .accountInfo(accountInfo)
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
            .costBasis(sharedHolding.getCostBasis())
            .marketValue(sharedHolding.getMarketValue())
            .unrealizedGainLoss(sharedHolding.getUnrealizedGainLoss())
            .unrealizedGainLossPercent(sharedHolding.getUnrealizedGainLossPercent())
            .sector(sharedHolding.getSector())
            .assetClass(parseAssetClass(sharedHolding.getAssetClass()))
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
