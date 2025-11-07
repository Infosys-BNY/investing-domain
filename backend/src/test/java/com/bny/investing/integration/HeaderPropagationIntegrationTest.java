package com.bny.investing.integration;

import com.bny.investing.client.RestLfdClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HeaderPropagationIntegrationTest extends IntegrationTestBase {
    
    @Autowired(required = false)
    private RestLfdClientService restLfdClientService;
    
    @Test
    void testRequiredHeadersAreSentToLfdApi() {
        assertNotNull(restLfdClientService, 
            "RestLfdClientService should be active (ensure app.mock.enabled=false)");
        
        String advisorId = "ADV001";
        
        try {
            var clients = restLfdClientService.getAdvisorClients(advisorId);
            assertNotNull(clients);
            System.out.println("✓ All required headers (X-User-ID, X-Advisor-ID, X-Request-ID, X-Timestamp) accepted by LFD API");
        } catch (Exception e) {
            fail("Header validation failed: " + e.getMessage());
        }
    }
    
    @Test
    void testLfdApiValidatesHeaders() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/internal/advisors/ADV001/clients?pageOffset=0&pageSize=50";
        
        try {
            restTemplate.getForEntity(url, String.class);
            fail("Expected validation exception for missing headers");
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError(), 
                "LFD API should reject request without headers with 4xx status");
            System.out.println("✓ LFD API correctly rejects requests without required headers");
        }
    }
    
    @Test
    void testLfdApiValidatesMissingXTimestamp() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/internal/advisors/ADV001/clients?pageOffset=0&pageSize=50";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-ID", "test-user");
        headers.set("X-Advisor-ID", "ADV001");
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            fail("Expected validation exception for missing X-Timestamp header");
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError(), 
                "LFD API should reject request without X-Timestamp header");
            System.out.println("✓ LFD API correctly validates X-Timestamp header");
        }
    }
}
