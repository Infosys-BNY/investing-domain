package com.bny.lfdapi.service;

import com.bny.shared.dto.request.ClientSearchRequest;
import com.bny.lfdapi.dto.response.AdvisorClientsResponse;
import com.bny.lfdapi.dto.response.ClientSearchResponse;
import com.bny.shared.dto.response.ClientDto;
import com.bny.shared.dto.common.StoredProcedureRequest;
import com.bny.shared.dto.common.StoredProcedureResponse;
import com.bny.shared.exception.DatabaseOperationException;
import com.bny.shared.service.StoredProcedureExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClientDataService {

    @Autowired
    private StoredProcedureExecutor storedProcedureExecutor;
    
    @Autowired
    private ObjectMapper objectMapper;

    public ClientSearchResponse searchClients(ClientSearchRequest request) {
        log.debug("Searching clients with request: {}", request);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_advisor_id", request.getAdvisorId());
        parameters.put("p_search_query", request.getSearchQuery());
        parameters.put("p_account_types", convertToJson(request.getAccountTypes()));
        parameters.put("p_min_market_value", request.getMinMarketValue());
        parameters.put("p_max_market_value", request.getMaxMarketValue());
        parameters.put("p_activity_status", request.getActivityStatus());
        parameters.put("p_risk_profile", request.getRiskProfile());
        parameters.put("p_sort_field", request.getSortField());
        parameters.put("p_sort_direction", request.getSortDirection());
        parameters.put("p_page_offset", request.getPageOffset());
        parameters.put("p_page_size", request.getPageSize());
        
        StoredProcedureRequest spRequest = StoredProcedureRequest.builder()
            .procedureName("sp_search_clients")
            .parameters(parameters)
            .build();
        
        StoredProcedureResponse spResponse = storedProcedureExecutor.execute(spRequest);
        
        List<ClientDto> clients = extractClientsFromResponse(spResponse);
        Integer totalCount = extractTotalCount(spResponse);
        
        return ClientSearchResponse.builder()
            .clients(clients)
            .totalCount(totalCount)
            .pageOffset(request.getPageOffset())
            .pageSize(request.getPageSize())
            .resultCode(spResponse.getResultCode())
            .errorMessage(spResponse.getErrorMessage())
            .build();
    }

    public AdvisorClientsResponse getAdvisorClients(String advisorId, Integer pageOffset, Integer pageSize) {
        log.debug("Getting clients for advisor: {} with page offset: {}, page size: {}", 
            advisorId, pageOffset, pageSize);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_advisor_id", advisorId);
        parameters.put("p_page_offset", pageOffset);
        parameters.put("p_page_size", pageSize);
        
        StoredProcedureRequest spRequest = StoredProcedureRequest.builder()
            .procedureName("sp_get_advisor_clients")
            .parameters(parameters)
            .build();
        
        StoredProcedureResponse spResponse = storedProcedureExecutor.execute(spRequest);
        
        List<ClientDto> clients = extractClientsFromResponse(spResponse);
        Integer totalCount = extractTotalCount(spResponse);
        
        return AdvisorClientsResponse.builder()
            .clients(clients)
            .totalCount(totalCount)
            .pageOffset(pageOffset)
            .pageSize(pageSize)
            .resultCode(spResponse.getResultCode())
            .errorMessage(spResponse.getErrorMessage())
            .build();
    }

    private List<ClientDto> extractClientsFromResponse(StoredProcedureResponse response) {
        if (response.getData() == null) {
            return new ArrayList<>();
        }
        
        try {
            if (response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) response.getData();
                
                List<ClientDto> clients = new ArrayList<>();
                for (Map<String, Object> row : resultList) {
                    ClientDto client = mapRowToClientDto(row);
                    clients.add(client);
                }
                return clients;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error extracting clients from stored procedure response", e);
            throw new DatabaseOperationException("Failed to parse client data", e);
        }
    }

    private ClientDto mapRowToClientDto(Map<String, Object> row) {
        return ClientDto.builder()
            .clientId((String) row.get("client_id"))
            .clientName((String) row.get("client_name"))
            .advisorId((String) row.get("advisor_id"))
            .advisorName((String) row.get("advisor_name"))
            .accountCount((Integer) row.get("account_count"))
            .totalMarketValue((java.math.BigDecimal) row.get("total_market_value"))
            .activityStatus((String) row.get("activity_status"))
            .riskProfile((String) row.get("risk_profile"))
            .lastActivityDate(convertToLocalDate(row.get("last_activity_date")))
            .createdDate(convertToLocalDateTime(row.get("created_date")))
            .build();
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

    private java.time.LocalDate convertToLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.time.LocalDate) {
            return (java.time.LocalDate) value;
        }
        return null;
    }

    private java.time.LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) value;
        }
        return null;
    }
}
