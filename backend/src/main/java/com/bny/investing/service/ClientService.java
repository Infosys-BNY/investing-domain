package com.bny.investing.service;

import com.bny.investing.client.LfdClientService;
import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.ClientSearchRequest;
import com.bny.investing.dto.PaginatedResponse;
import com.bny.investing.model.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final LfdClientService lfdClientService;

    @Cacheable(value = "clients", key = "#advisorId + '_' + #page + '_' + #size")
    public PaginatedResponse<ClientDto> getAdvisorClients(String advisorId, int page, int size) {
        List<ClientDto> allClients = lfdClientService.getAdvisorClients(advisorId);
        
        int start = page * size;
        int end = Math.min(start + size, allClients.size());
        
        List<ClientDto> pageContent = allClients.subList(start, end);
        
        return PaginatedResponse.<ClientDto>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(allClients.size())
                .totalPages((int) Math.ceil((double) allClients.size() / size))
                .first(page == 0)
                .last(end >= allClients.size())
                .build();
    }

    @Cacheable(value = "client-search", key = "#request.hashCode()")
    public PaginatedResponse<ClientDto> searchClients(ClientSearchRequest request) {
        List<ClientDto> allClients = lfdClientService.getAdvisorClients(request.getAdvisorId());
        
        List<ClientDto> filteredClients = allClients.stream()
                .filter(client -> filterByClientName(client, request.getClientName()))
                .filter(client -> filterByAccountNumber(client, request.getAccountNumber()))
                .filter(client -> filterByTaxId(client, request.getTaxId()))
                .filter(client -> filterByAccountTypes(client, request.getAccountTypes()))
                .filter(client -> filterByRiskProfiles(client, request.getRiskProfiles()))
                .filter(client -> filterByPerformance(client, request.getPerformanceFilter()))
                .sorted(getComparator(request))
                .collect(Collectors.toList());
        
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), filteredClients.size());
        
        List<ClientDto> pageContent = start < filteredClients.size() 
                ? filteredClients.subList(start, end) 
                : List.of();
        
        return PaginatedResponse.<ClientDto>builder()
                .content(pageContent)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(filteredClients.size())
                .totalPages((int) Math.ceil((double) filteredClients.size() / request.getSize()))
                .first(request.getPage() == 0)
                .last(end >= filteredClients.size())
                .build();
    }

    private boolean filterByClientName(ClientDto client, String clientName) {
        return clientName == null || clientName.isBlank() || 
               client.getClientName().toLowerCase().contains(clientName.toLowerCase());
    }

    private boolean filterByAccountNumber(ClientDto client, String accountNumber) {
        return accountNumber == null || accountNumber.isBlank() ||
               client.getAccounts().stream()
                       .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
    }

    private boolean filterByTaxId(ClientDto client, String taxId) {
        return taxId == null || taxId.isBlank() || 
               client.getTaxId().equals(taxId);
    }

    private boolean filterByAccountTypes(ClientDto client, List<?> accountTypes) {
        return accountTypes == null || accountTypes.isEmpty() ||
               client.getAccounts().stream()
                       .anyMatch(acc -> accountTypes.contains(acc.getAccountType()));
    }

    private boolean filterByRiskProfiles(ClientDto client, List<?> riskProfiles) {
        return riskProfiles == null || riskProfiles.isEmpty() ||
               riskProfiles.contains(client.getRiskProfile());
    }

    private boolean filterByPerformance(ClientDto client, Object performanceFilter) {
        if (performanceFilter == null) {
            return true;
        }
        return true;
    }

    private Comparator<ClientDto> getComparator(ClientSearchRequest request) {
        Comparator<ClientDto> comparator = switch (request.getSortBy()) {
            case CLIENT_NAME -> Comparator.comparing(ClientDto::getClientName);
            case MARKET_VALUE -> Comparator.comparing(ClientDto::getTotalMarketValue);
            case LAST_ACTIVITY -> Comparator.comparing(ClientDto::getLastAccessed);
            case YTD_PERFORMANCE -> Comparator.comparing(ClientDto::getYtdPerformance);
        };
        
        return request.getSortDirection() == SortDirection.DESC 
                ? comparator.reversed() 
                : comparator;
    }
}
