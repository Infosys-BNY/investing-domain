package com.bny.lfdapi.integration;

import com.bny.lfdapi.dto.response.ClientSearchResponse;
import com.bny.lfdapi.dto.response.AdvisorClientsResponse;
import com.bny.lfdapi.service.ClientDataService;
import com.bny.shared.dto.request.ClientSearchRequest;
import com.bny.shared.dto.response.ClientDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClientDataService Integration Tests")
public class ClientDataServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ClientDataService clientDataService;

    @Test
    @DisplayName("Should successfully connect to AWS RDS and retrieve clients for advisor")
    public void searchClients_WithValidAdvisorId_ReturnsClients() {
        String advisorId = ADVISOR_ID_1;
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        ClientSearchResponse response = measureExecutionTimeWithResult(
            () -> clientDataService.searchClients(request),
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isNotEmpty();
        assertThat(response.getTotalCount()).isGreaterThan(0);
        
        log.info("Retrieved {} clients for advisor {}", response.getClients().size(), advisorId);
        
        ClientDto firstClient = response.getClients().get(0);
        assertThat(firstClient.getClientId()).isNotNull();
        assertThat(firstClient.getClientName()).isNotNull();
        
        assertPerformanceTarget(metrics.getAverageExecutionTime(), STORED_PROCEDURE_TARGET_MS, "searchClients");
    }

    @Test
    @DisplayName("Should filter clients by search query")
    public void searchClients_WithSearchQuery_FiltersResults() {
        String advisorId = ADVISOR_ID_1;
        String searchQuery = "Smith";
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .searchQuery(searchQuery)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ClientSearchResponse response = clientDataService.searchClients(request);

        assertThat(response).isNotNull();
        log.info("Search for '{}' returned {} clients", searchQuery, response.getClients().size());
        
        if (!response.getClients().isEmpty()) {
            boolean foundMatch = response.getClients().stream()
                .anyMatch(client -> client.getClientName().contains(searchQuery));
            assertThat(foundMatch).isTrue();
        }
    }

    @Test
    @DisplayName("Should retrieve advisor clients using sp_get_advisor_clients")
    public void getAdvisorClients_WithValidAdvisorId_ReturnsClients() {
        String advisorId = ADVISOR_ID_1;
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        AdvisorClientsResponse response = measureExecutionTimeWithResult(
            () -> clientDataService.getAdvisorClients(advisorId, 0, 50),
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isNotEmpty();
        assertThat(response.getTotalCount()).isGreaterThan(0);
        
        log.info("Retrieved {} clients for advisor {} using getAdvisorClients", 
            response.getClients().size(), advisorId);
        
        response.getClients().forEach(client -> {
            assertThat(client.getClientId()).isNotNull();
            assertThat(client.getClientName()).isNotNull();
            assertThat(client.getAdvisorId()).isEqualTo(advisorId);
        });
        
        assertPerformanceTarget(metrics.getAverageExecutionTime(), STORED_PROCEDURE_TARGET_MS, "getAdvisorClients");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    public void getAdvisorClients_WithPagination_ReturnsPaginatedResults() {
        String advisorId = ADVISOR_ID_1;
        int pageSize = 2;
        
        AdvisorClientsResponse page1 = clientDataService.getAdvisorClients(advisorId, 0, pageSize);
        
        assertThat(page1).isNotNull();
        assertThat(page1.getPageOffset()).isEqualTo(0);
        assertThat(page1.getPageSize()).isEqualTo(pageSize);
        
        if (page1.getTotalCount() > pageSize) {
            AdvisorClientsResponse page2 = clientDataService.getAdvisorClients(advisorId, pageSize, pageSize);
            assertThat(page2).isNotNull();
            assertThat(page2.getPageOffset()).isEqualTo(pageSize);
            
            List<String> page1Ids = page1.getClients().stream()
                .map(ClientDto::getClientId)
                .toList();
            List<String> page2Ids = page2.getClients().stream()
                .map(ClientDto::getClientId)
                .toList();
            
            assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
            
            log.info("Pagination test: Page 1 has {} clients, Page 2 has {} clients", 
                page1.getClients().size(), page2.getClients().size());
        }
    }

    @Test
    @DisplayName("Should return empty results for invalid advisor ID")
    public void searchClients_WithInvalidAdvisorId_ReturnsEmptyResults() {
        String invalidAdvisorId = "INVALID_ADV_999";
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(invalidAdvisorId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ClientSearchResponse response = clientDataService.searchClients(request);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
        
        log.info("Invalid advisor ID returned empty results as expected");
    }

    @Test
    @DisplayName("Should meet performance targets for stored procedure execution")
    public void searchClients_PerformanceTest_MeetsTargets() {
        String advisorId = ADVISOR_ID_1;
        int iterations = 20;
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        
        for (int i = 0; i < iterations; i++) {
            try {
                measureExecutionTimeWithResult(
                    () -> clientDataService.searchClients(request),
                    metrics
                );
            } catch (Exception e) {
                metrics.recordFailure();
                log.error("Execution {} failed", i, e);
            }
        }
        
        double p95 = metrics.getPercentile(95);
        double successRate = metrics.getSuccessRate();
        long avgTime = metrics.getAverageExecutionTime();
        
        log.info("Performance Test Results:");
        log.info("  Total Executions: {}", iterations);
        log.info("  Successful: {} ({} %)", metrics.getSuccessCount(), successRate * 100);
        log.info("  Average Time: {} ms", avgTime);
        log.info("  95th Percentile: {} ms", p95);
        
        assertThat(successRate)
            .as("Success rate should be greater than %s", SUCCESS_RATE_TARGET)
            .isGreaterThanOrEqualTo(SUCCESS_RATE_TARGET);
        
        if (p95 > STORED_PROCEDURE_TARGET_MS) {
            log.warn("95th percentile ({} ms) exceeds target ({} ms)", p95, STORED_PROCEDURE_TARGET_MS);
        }
    }

    @Test
    @DisplayName("Should handle null search parameters gracefully")
    public void searchClients_WithNullParameters_ReturnsAllClients() {
        String advisorId = ADVISOR_ID_1;
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ClientSearchResponse response = clientDataService.searchClients(request);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isNotEmpty();
        
        log.info("Null parameters handled correctly, returned {} clients", response.getClients().size());
    }

    @Test
    @DisplayName("Should map all DTO fields correctly from database")
    public void searchClients_VerifiesDtoMapping() {
        String advisorId = ADVISOR_ID_1;
        
        ClientSearchRequest request = ClientSearchRequest.builder()
            .advisorId(advisorId)
            .pageOffset(0)
            .pageSize(50)
            .build();
        
        ClientSearchResponse response = clientDataService.searchClients(request);

        assertThat(response).isNotNull();
        assertThat(response.getClients()).isNotEmpty();
        
        ClientDto client = response.getClients().get(0);
        
        assertThat(client.getClientId()).isNotNull().isNotEmpty();
        assertThat(client.getClientName()).isNotNull().isNotEmpty();
        assertThat(client.getAdvisorId()).isNotNull().isEqualTo(advisorId);
        
        log.info("DTO mapping verified: clientId={}, clientName={}, advisorId={}", 
            client.getClientId(), client.getClientName(), client.getAdvisorId());
    }
}
