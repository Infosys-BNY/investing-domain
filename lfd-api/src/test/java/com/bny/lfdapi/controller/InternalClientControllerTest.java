package com.bny.lfdapi.controller;

import com.bny.shared.dto.request.ClientSearchRequest;
import com.bny.lfdapi.dto.response.AdvisorClientsResponse;
import com.bny.shared.dto.response.ClientDto;
import com.bny.lfdapi.dto.response.ClientSearchResponse;
import com.bny.lfdapi.service.ClientDataService;
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
class InternalClientControllerTest {

    @Mock
    private ClientDataService clientDataService;

    @InjectMocks
    private InternalClientController controller;

    private ClientSearchRequest searchRequest;
    private ClientSearchResponse searchResponse;
    private AdvisorClientsResponse advisorClientsResponse;

    @BeforeEach
    void setUp() {
        searchRequest = ClientSearchRequest.builder()
            .advisorId("advisor123")
            .searchQuery("Smith")
            .pageOffset(0)
            .pageSize(50)
            .build();

        List<ClientDto> clients = Arrays.asList(
            ClientDto.builder()
                .clientId("client1")
                .clientName("John Smith")
                .advisorId("advisor123")
                .totalMarketValue(new BigDecimal("1000000"))
                .build(),
            ClientDto.builder()
                .clientId("client2")
                .clientName("Jane Smith")
                .advisorId("advisor123")
                .totalMarketValue(new BigDecimal("750000"))
                .build()
        );

        searchResponse = ClientSearchResponse.builder()
            .clients(clients)
            .totalCount(2)
            .pageOffset(0)
            .pageSize(50)
            .resultCode(0)
            .build();

        advisorClientsResponse = AdvisorClientsResponse.builder()
            .clients(clients)
            .totalCount(2)
            .pageOffset(0)
            .pageSize(50)
            .resultCode(0)
            .build();
    }

    @Test
    void searchClients_Success() {
        when(clientDataService.searchClients(any(ClientSearchRequest.class)))
            .thenReturn(searchResponse);

        ResponseEntity<ClientSearchResponse> response = controller.searchClients(searchRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getClients()).hasSize(2);
        assertThat(response.getBody().getTotalCount()).isEqualTo(2);
        assertThat(response.getBody().getResultCode()).isEqualTo(0);
        
        verify(clientDataService).searchClients(searchRequest);
    }

    @Test
    void getAdvisorClients_Success() {
        when(clientDataService.getAdvisorClients(eq("advisor123"), eq(0), eq(50)))
            .thenReturn(advisorClientsResponse);

        ResponseEntity<AdvisorClientsResponse> response = 
            controller.getAdvisorClients("advisor123", 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getClients()).hasSize(2);
        assertThat(response.getBody().getTotalCount()).isEqualTo(2);
        
        verify(clientDataService).getAdvisorClients("advisor123", 0, 50);
    }

    @Test
    void getAdvisorClients_WithDefaultPagination() {
        when(clientDataService.getAdvisorClients(eq("advisor123"), eq(0), eq(50)))
            .thenReturn(advisorClientsResponse);

        ResponseEntity<AdvisorClientsResponse> response = 
            controller.getAdvisorClients("advisor123", 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(clientDataService).getAdvisorClients("advisor123", 0, 50);
    }

    @Test
    void searchClients_NonZeroResultCode() {
        ClientSearchResponse errorResponse = ClientSearchResponse.builder()
            .clients(Arrays.asList())
            .totalCount(0)
            .pageOffset(0)
            .pageSize(50)
            .resultCode(1)
            .errorMessage("Database error")
            .build();

        when(clientDataService.searchClients(any(ClientSearchRequest.class)))
            .thenReturn(errorResponse);

        ResponseEntity<ClientSearchResponse> response = controller.searchClients(searchRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResultCode()).isEqualTo(1);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Database error");
    }
}
