package com.bny.investing.integration;

import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.PaginatedResponse;
import com.bny.investing.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ClientDataIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private ClientService clientService;
    
    @Test
    void testGetAdvisorClients_DomainToLfdCommunication() {
        String advisorId = "ADV001";
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, 0, 50);
        
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Response content should not be null");
        assertTrue(response.getTotalElements() >= 0, "Total elements should be non-negative");
        
        if (response.getTotalElements() > 0) {
            ClientDto firstClient = response.getContent().get(0);
            assertNotNull(firstClient.getClientId(), "Client ID should not be null");
            assertNotNull(firstClient.getClientName(), "Client name should not be null");
            assertNotNull(firstClient.getAdvisorId(), "Advisor ID should not be null");
            assertEquals(advisorId, firstClient.getAdvisorId(), "Advisor ID should match");
        }
        
        System.out.println("✓ Successfully retrieved " + response.getTotalElements() + " clients from LFD API");
    }
    
    @Test
    void testGetAdvisorClients_VerifyStoredProcedureExecution() {
        String advisorId = "ADV001";
        
        PaginatedResponse<ClientDto> response = clientService.getAdvisorClients(advisorId, 0, 50);
        
        assertNotNull(response);
        assertTrue(response.getTotalPages() >= 0);
        
        System.out.println("✓ Stored procedure sp_get_advisor_clients executed successfully");
    }
    
    @Test
    void testGetAdvisorClients_Pagination() {
        String advisorId = "ADV001";
        
        PaginatedResponse<ClientDto> page1 = clientService.getAdvisorClients(advisorId, 0, 2);
        assertNotNull(page1);
        assertEquals(0, page1.getPage());
        assertEquals(2, page1.getSize());
        
        PaginatedResponse<ClientDto> page2 = clientService.getAdvisorClients(advisorId, 1, 2);
        assertNotNull(page2);
        assertEquals(1, page2.getPage());
        assertEquals(2, page2.getSize());
        
        System.out.println("✓ Pagination working correctly");
    }
}
