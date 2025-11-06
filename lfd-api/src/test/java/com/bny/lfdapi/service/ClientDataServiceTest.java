package com.bny.lfdapi.service;

import com.bny.shared.dto.request.ClientSearchRequest;
import com.bny.lfdapi.dto.response.AdvisorClientsResponse;
import com.bny.lfdapi.dto.response.ClientSearchResponse;
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
class ClientDataServiceTest {

    @Mock
    private StoredProcedureExecutor storedProcedureExecutor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ClientDataService clientDataService;

    private ClientSearchRequest searchRequest;
    private List<Map<String, Object>> mockClientData;

    @BeforeEach
    void setUp() {
        searchRequest = ClientSearchRequest.builder()
            .advisorId("advisor123")
            .searchQuery("Smith")
            .pageOffset(0)
            .pageSize(50)
            .build();

        mockClientData = new ArrayList<>();
        Map<String, Object> client1 = new HashMap<>();
        client1.put("client_id", "client1");
        client1.put("client_name", "John Smith");
        client1.put("advisor_id", "advisor123");
        client1.put("advisor_name", "Bob Advisor");
        client1.put("account_count", 3);
        client1.put("total_market_value", new BigDecimal("1000000"));
        client1.put("activity_status", "Active");
        client1.put("risk_profile", "Moderate");
        mockClientData.add(client1);
    }

    @Test
    void searchClients_Success() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_count", 1);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(mockClientData)
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        ClientSearchResponse response = clientDataService.searchClients(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).hasSize(1);
        assertThat(response.getClients().get(0).getClientId()).isEqualTo("client1");
        assertThat(response.getClients().get(0).getClientName()).isEqualTo("John Smith");
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getResultCode()).isEqualTo(0);
        
        verify(storedProcedureExecutor).execute(any(StoredProcedureRequest.class));
    }

    @Test
    void getAdvisorClients_Success() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_count", 1);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(mockClientData)
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        AdvisorClientsResponse response = 
            clientDataService.getAdvisorClients("advisor123", 0, 50);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).hasSize(1);
        assertThat(response.getClients().get(0).getClientId()).isEqualTo("client1");
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getResultCode()).isEqualTo(0);
        
        verify(storedProcedureExecutor).execute(any(StoredProcedureRequest.class));
    }

    @Test
    void searchClients_EmptyResults() {
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("p_total_count", 0);
        
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(new ArrayList<>())
            .outputParameters(outputParams)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        ClientSearchResponse response = clientDataService.searchClients(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    void searchClients_NullData() {
        StoredProcedureResponse spResponse = StoredProcedureResponse.builder()
            .resultCode(0)
            .data(null)
            .build();

        when(storedProcedureExecutor.execute(any(StoredProcedureRequest.class)))
            .thenReturn(spResponse);

        ClientSearchResponse response = clientDataService.searchClients(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isEmpty();
    }
}
