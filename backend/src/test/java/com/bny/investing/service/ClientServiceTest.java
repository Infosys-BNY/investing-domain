package com.bny.investing.service;

import com.bny.investing.client.LfdClientService;
import com.bny.investing.dto.AccountDto;
import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.ClientSearchRequest;
import com.bny.investing.dto.PaginatedResponse;
import com.bny.investing.exception.ResourceNotFoundException;
import com.bny.investing.exception.UnauthorizedException;
import com.bny.investing.model.AccountType;
import com.bny.investing.model.RiskProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private LfdClientService lfdClientService;

    @InjectMocks
    private ClientService clientService;

    private List<ClientDto> mockClients;

    @BeforeEach
    void setUp() {
        mockClients = createMockClients();
    }

    @Test
    void testGetAdvisorClients_Success() {
        String advisorId = "advisor123";
        int page = 0;
        int size = 50;
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, page, size);
        
        assertNotNull(response);
        assertEquals(3, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(50, response.getSize());
        assertEquals(3, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        verify(lfdClientService).getAdvisorClients(advisorId);
    }

    @Test
    void testGetAdvisorClients_EmptyResult() {
        String advisorId = "advisor999";
        int page = 0;
        int size = 50;
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(List.of());
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, page, size);
        
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
        verify(lfdClientService).getAdvisorClients(advisorId);
    }

    @Test
    void testGetAdvisorClients_WithPagination() {
        String advisorId = "advisor123";
        int page = 1;
        int size = 2;
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, page, size);
        
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    @Test
    void testSearchClients_Success() {
        String advisorId = "advisor123";
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .clientName("Smith")
            .page(0)
            .size(50)
            .build();
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.searchClients(request);
        
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("John Smith", response.getContent().get(0).getClientName());
        verify(lfdClientService).getAdvisorClients(advisorId);
    }

    @Test
    void testSearchClients_ByAccountTypes() {
        String advisorId = "advisor123";
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .accountTypes(List.of(AccountType.INDIVIDUAL))
            .page(0)
            .size(50)
            .build();
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.searchClients(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().size() > 0);
        assertTrue(response.getContent().stream()
            .allMatch(c -> c.getAccounts().stream()
                .anyMatch(a -> a.getAccountType() == AccountType.INDIVIDUAL)));
    }

    @Test
    void testSearchClients_ByRiskProfiles() {
        String advisorId = "advisor123";
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .riskProfiles(List.of(RiskProfile.MODERATE))
            .page(0)
            .size(50)
            .build();
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.searchClients(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().size() > 0);
        assertTrue(response.getContent().stream()
            .allMatch(c -> c.getRiskProfile() == RiskProfile.MODERATE));
    }

    @Test
    void testSearchClients_NoMatches() {
        String advisorId = "advisor123";
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .clientName("NonExistent")
            .page(0)
            .size(50)
            .build();
        
        when(lfdClientService.getAdvisorClients(advisorId)).thenReturn(mockClients);
        
        PaginatedResponse<ClientDto> response = clientService.searchClients(request);
        
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }

    private List<ClientDto> createMockClients() {
        AccountDto account1 = AccountDto.builder()
            .accountId("acc1")
            .accountNumber("12345")
            .accountType(AccountType.INDIVIDUAL)
            .marketValue(new BigDecimal("150000"))
            .build();
            
        AccountDto account2 = AccountDto.builder()
            .accountId("acc2")
            .accountNumber("67890")
            .accountType(AccountType.IRA)
            .marketValue(new BigDecimal("100000"))
            .build();
            
        AccountDto account3 = AccountDto.builder()
            .accountId("acc3")
            .accountNumber("11111")
            .accountType(AccountType.TRUST)
            .marketValue(new BigDecimal("500000"))
            .build();

        ClientDto client1 = ClientDto.builder()
            .clientId("client1")
            .clientName("John Smith")
            .advisorId("advisor123")
            .riskProfile(RiskProfile.MODERATE)
            .totalMarketValue(new BigDecimal("250000"))
            .accounts(Arrays.asList(account1, account2))
            .build();

        ClientDto client2 = ClientDto.builder()
            .clientId("client2")
            .clientName("Sarah Johnson")
            .advisorId("advisor123")
            .riskProfile(RiskProfile.AGGRESSIVE)
            .totalMarketValue(new BigDecimal("500000"))
            .accounts(List.of(account3))
            .build();

        ClientDto client3 = ClientDto.builder()
            .clientId("client3")
            .clientName("Michael Williams")
            .advisorId("advisor123")
            .riskProfile(RiskProfile.CONSERVATIVE)
            .totalMarketValue(new BigDecimal("150000"))
            .accounts(List.of(account1))
            .build();

        return Arrays.asList(client1, client2, client3);
    }
}
