# Product Requirements Document: SpringBoot LFD API Layer

## 1. Executive Summary

### Product Overview
The LFD (Logical File Distribution) API Layer is the intermediate Spring Boot application that orchestrates database operations between the Domain API Layer and the Data Services Shared Resource. This layer handles internal API calls, manages database transactions, and abstracts the complexity of stored procedure execution and data access patterns.

### Business Objective
Provide a robust, scalable, and efficient data orchestration layer that manages all database interactions while maintaining transaction integrity, connection pooling optimization, and performance monitoring for the BNY Advisor Portal backend architecture.

### Target Users
- Primary: Domain API Layer (internal service)
- Secondary: Data Services Shared Resource (database abstraction layer)
- Tertiary: System administrators and DevOps teams

## 2. User Stories

### Primary User Stories
1. **As the Domain API Layer**, I need to retrieve client data through internal APIs, so that I can serve frontend requests without direct database access.
2. **As the Domain API Layer**, I need to execute complex holdings queries, so that I can provide portfolio data with calculated fields and performance metrics.
3. **As the Domain API Layer**, I need to initiate export jobs asynchronously, so that large data exports don't block user interactions.
4. **As a system administrator**, I need comprehensive transaction monitoring, so that I can ensure data consistency and performance.
5. **As a developer**, I need clear stored procedure interfaces, so that database operations are predictable and maintainable.

## 3. Functional Requirements

### 3.1 Internal API Endpoints

#### 3.1.1 Client Data Orchestration
- `POST /internal/clients/search`
  - Execute client search with complex filtering criteria
  - Call stored procedure `sp_search_clients` with parameter mapping
  - Return paginated results with advisor-specific filtering
  
- `GET /internal/advisors/{advisorId}/clients`
  - Retrieve advisor's assigned client list
  - Execute stored procedure `sp_get_advisor_clients`
  - Include account summaries and performance metrics
  
- `POST /internal/audit/log-access`
  - Log client access events for compliance
  - Execute stored procedure `sp_log_client_access`
  - Ensure audit trail integrity with transaction logging

#### 3.1.2 Holdings Data Orchestration
- `POST /internal/accounts/{accountId}/holdings`
  - Retrieve comprehensive holdings data
  - Execute stored procedure `sp_get_account_holdings`
  - Include calculated fields (unrealized gains, portfolio percentages)
  
- `GET /internal/accounts/{accountId}/summary`
  - Get portfolio summary metrics
  - Execute stored procedure `sp_get_portfolio_summary`
  - Cache results for 5 minutes to optimize performance
  
- `GET /internal/holdings/{symbol}/taxlots`
  - Retrieve detailed tax lot information
  - Execute stored procedure `sp_get_tax_lots`
  - Include cost basis calculations and holding period analysis

#### 3.1.3 Export Job Management
- `POST /internal/export/holdings`
  - Initiate asynchronous export job
  - Execute stored procedure `sp_create_export_job`
  - Return job ID immediately for status polling
  - Queue actual data processing for background execution
  
- `GET /internal/export/jobs/{jobId}/status`
  - Check export job progress
  - Execute stored procedure `sp_get_export_job_status`
  - Return completion percentage and download URL when ready

### 3.2 Market Data Integration

#### 3.2.1 Price Data Orchestration
- `POST /internal/securities/prices/batch`
  - Retrieve batch pricing for multiple securities
  - Execute stored procedure `sp_get_security_prices`
  - Support both real-time and delayed pricing based on user permissions
  - **Architecture Decision**: LFD provides price data to Domain, Domain does not call external market data services directly

#### 3.2.2 Price Update Processing
- `POST /internal/prices/update-cache`
  - Update price cache with latest market data
  - Execute stored procedure `sp_update_price_cache`
  - Trigger cache invalidation in Domain API layer

### 3.3 Authentication and Authorization

#### 3.3.1 Trusted Header Validation
- **Header Propagation**: Receive user context from Domain API via trusted headers
  - `X-User-ID`: Authenticated user identifier
  - `X-Advisor-ID`: Advisor identifier for client access validation
  - `X-Request-ID`: Correlation ID for distributed tracing
  - `X-Timestamp`: Request timestamp for replay attack prevention

#### 3.3.2 Internal Security
- **Network Security**: Only accept requests from Domain API IP ranges
- **Header Validation**: Validate all required headers and signatures
- **Request Signing**: HMAC-SHA256 signature verification for internal APIs
- **Rate Limiting**: Internal rate limiting to prevent abuse

## 4. Stored Procedure Specifications

### 4.1 Client Management Procedures

#### 4.1.1 sp_search_clients
```sql
CREATE PROCEDURE sp_search_clients(
    IN p_advisor_id VARCHAR(50),
    IN p_search_query VARCHAR(255),
    IN p_account_types JSON,
    IN p_min_market_value DECIMAL(15,2),
    IN p_max_market_value DECIMAL(15,2),
    IN p_performance_filter VARCHAR(20),
    IN p_activity_status VARCHAR(20),
    IN p_risk_profiles JSON,
    IN p_activity_start_date DATETIME,
    IN p_activity_end_date DATETIME,
    IN p_sort_field VARCHAR(50),
    IN p_sort_direction VARCHAR(10),
    IN p_page_offset INT,
    IN p_page_size INT,
    OUT p_total_count INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Complex client search with filtering and pagination
    -- Returns JSON result set with client and account information
END
```

#### 4.1.2 sp_get_advisor_clients
```sql
CREATE PROCEDURE sp_get_advisor_clients(
    IN p_advisor_id VARCHAR(50),
    IN p_page_offset INT,
    IN p_page_size INT,
    OUT p_total_count INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Retrieve paginated list of clients assigned to advisor
    -- Include account summaries and key metrics
END
```

#### 4.1.3 sp_log_client_access
```sql
CREATE PROCEDURE sp_log_client_access(
    IN p_user_id VARCHAR(50),
    IN p_advisor_id VARCHAR(50),
    IN p_client_id VARCHAR(50),
    IN p_account_id VARCHAR(50),
    IN p_access_type VARCHAR(20),
    IN p_request_id VARCHAR(100),
    IN p_timestamp DATETIME,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Log client access for audit and compliance
    -- Ensure atomic transaction with proper error handling
END
```

### 4.2 Holdings Management Procedures

#### 4.2.1 sp_get_account_holdings
```sql
CREATE PROCEDURE sp_get_account_holdings(
    IN p_account_id VARCHAR(50),
    IN p_filter_json JSON,
    IN p_include_calculated_fields BOOLEAN,
    IN p_pricing_level VARCHAR(20), -- 'delayed' or 'realtime'
    OUT p_total_count INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Retrieve comprehensive holdings with calculated fields
    -- Join positions, securities, prices, and reference data
    -- Calculate unrealized gains, portfolio percentages
END
```

#### 4.2.2 sp_get_portfolio_summary
```sql
CREATE PROCEDURE sp_get_portfolio_summary(
    IN p_account_id VARCHAR(50),
    IN p_as_of_date DATETIME,
    OUT p_total_market_value DECIMAL(15,2),
    OUT p_total_cost_basis DECIMAL(15,2),
    OUT p_total_unrealized_gl DECIMAL(15,2),
    OUT p_total_realized_gl_ytd DECIMAL(15,2),
    OUT p_number_of_holdings INT,
    OUT p_portfolio_beta DECIMAL(10,6),
    OUT p_dividend_yield DECIMAL(8,4),
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Calculate portfolio-level summary metrics
    -- Include performance analytics and risk measures
END
```

#### 4.2.3 sp_get_tax_lots
```sql
CREATE PROCEDURE sp_get_tax_lots(
    IN p_account_id VARCHAR(50),
    IN p_security_symbol VARCHAR(20),
    IN p_cost_basis_method VARCHAR(10), -- 'FIFO', 'LIFO', 'HIFO'
    OUT p_tax_lots JSON,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Retrieve detailed tax lot information
    -- Calculate holding periods and tax impact
    -- Support multiple cost basis methods
END
```

### 4.3 Export Management Procedures

#### 4.3.1 sp_create_export_job
```sql
CREATE PROCEDURE sp_create_export_job(
    IN p_account_id VARCHAR(50),
    IN p_export_format VARCHAR(10), -- 'EXCEL', 'CSV', 'PDF'
    IN p_filter_criteria JSON,
    IN p_requested_by VARCHAR(50),
    OUT p_job_id VARCHAR(50),
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Create export job record and return job ID
    -- Queue job for background processing
    -- Set initial status to 'PENDING'
END
```

#### 4.3.2 sp_get_export_job_status
```sql
CREATE PROCEDURE sp_get_export_job_status(
    IN p_job_id VARCHAR(50),
    OUT p_status VARCHAR(20),
    OUT p_progress_percent INT,
    OUT p_download_url VARCHAR(500),
    OUT p_error_message VARCHAR(500),
    OUT p_result_code INT
)
BEGIN
    -- Retrieve export job status and progress
    -- Return download URL when job is complete
END
```

### 4.4 Market Data Procedures

#### 4.4.1 sp_get_security_prices
```sql
CREATE PROCEDURE sp_get_security_prices(
    IN p_symbols JSON,
    IN p_pricing_level VARCHAR(20),
    IN p_user_permissions JSON,
    OUT p_prices JSON,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Retrieve current prices for specified securities
    -- Apply user permission level for real-time vs delayed pricing
    -- Include price change calculations
END
```

#### 4.4.2 sp_update_price_cache
```sql
CREATE PROCEDURE sp_update_price_cache(
    IN p_price_updates JSON,
    IN p_update_source VARCHAR(50),
    OUT p_updated_count INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    -- Update price cache with latest market data
    -- Trigger cache invalidation notifications
    -- Maintain price history for analytics
END
```

## 5. Data Models and Internal DTOs

### 5.1 Request DTOs
```java
public class ClientSearchRequest {
    private String advisorId;
    private String searchQuery;
    private List<String> accountTypes;
    private BigDecimal minMarketValue;
    private BigDecimal maxMarketValue;
    private String performanceFilter;
    private String activityStatus;
    private List<String> riskProfiles;
    private LocalDateTime activityStartDate;
    private LocalDateTime activityEndDate;
    private String sortField;
    private String sortDirection;
    private int pageOffset;
    private int pageSize;
}

public class HoldingsRequest {
    private String accountId;
    private HoldingFilter filter;
    private boolean includeCalculatedFields;
    private String pricingLevel;
}

public class ExportRequest {
    private String accountId;
    private String exportFormat;
    private HoldingFilter filterCriteria;
    private String requestedBy;
}
```

### 5.2 Response DTOs
```java
public class StoredProcedureResponse {
    private int resultCode;
    private String errorMessage;
    private Object data;
    private Map<String, Object> outputParameters;
}

public class ClientSearchResponse {
    private List<ClientDto> clients;
    private int totalCount;
    private int resultCode;
    private String errorMessage;
}

public class HoldingsResponse {
    private List<HoldingDto> holdings;
    private PortfolioSummaryDto summary;
    private int totalCount;
    private int resultCode;
    private String errorMessage;
}
```

### 5.3 Internal Communication DTOs
```java
public class InternalRequestContext {
    private String userId;
    private String advisorId;
    private String requestId;
    private LocalDateTime timestamp;
    private String signature;
    private String clientIp;
}

public class DatabaseOperationResult {
    private boolean success;
    private int affectedRows;
    private Object resultData;
    private String errorCode;
    private String errorMessage;
    private long executionTimeMs;
}
```

## 6. Transaction Management

### 6.1 Transaction Boundaries
- **Read Operations**: Read-only transactions with appropriate isolation levels
- **Write Operations**: ACID-compliant transactions with rollback capabilities
- **Batch Operations**: Transaction batching for performance optimization
- **Cross-Procedure Transactions**: Coordinate transactions across multiple stored procedures

### 6.2 Isolation Levels
```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
 {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource);
        manager.setDefaultTimeout(30); // 30 seconds
        return manager;
    }
    
    // Read operations use READ_COMMITTED
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<ClientDto> searchClients(ClientSearchRequest request) {
        // Implementation
    }
    
    // Write operations use SERIALIZABLE for critical data
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void logClientAccess(AccessLogEntry logEntry) {
 {
        // Implementation
    }
}
```

### 6.3 Rollback Scenarios
- **Database Errors**: Automatic rollback on SQL exceptions
- **Business Rule Violations**: Programmatic rollback with custom exceptions
- **Timeout Scenarios**: Rollback on transaction timeout
- **Connection Failures**: Rollback and retry with circuit breaker

## 7. Connection Pooling Configuration

### 7.1 HikariCP Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000  # 5 minutes
      max-lifetime: 1800000  # 30 minutes
      connection-timeout: 20000  # 20 seconds
      leak-detection-threshold: 60000  # 1 minute
      pool-name: LFD-API-POOL
      connection-test-query: SELECT 1
      validation-timeout: 3000  # 3 seconds
```

### 7.2 Connection Pool Monitoring
- **Active Connections**: Real-time monitoring of active connection count
- **Pool Utilization**: Track pool usage patterns and optimize sizing
- **Connection Leaks**: Automatic detection and alerting for connection leaks
- **Performance Metrics**: Connection acquisition time and wait time tracking

### 7.3 Multi-Database Support
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.readonly")
    public DataSource readOnlyDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("primary", primaryDataSource());
        dataSourceMap.put("readonly", readOnlyDataSource());
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource());
        return routingDataSource;
    }
}
```

## 8. Error Handling and Resilience

### 8.1 Database Error Handling
```java
@Component
public class DatabaseErrorHandler {
    
    @Retryable(value = {DataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public StoredProcedureResponse executeWithRetry(Callable<StoredProcedureResponse> operation) {
        try {
            return operation.call();
        } catch (DataAccessException e) {
            log.error("Database operation failed, retrying...", e);
            throw e;
        }
    }
    
    @Recover
    public StoredProcedureResponse recover(DataAccessException e, Callable<StoredProcedureResponse> operation) {
        log.error("All retry attempts exhausted for database operation", e);
        return StoredProcedureResponse.error("DB_ERROR", "Database operation failed after retries");
    }
}
```

### 8.2 Circuit Breaker Configuration
```java
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreaker databaseCircuitBreaker() {
        return CircuitBreaker.ofDefaults("database");
    }
    
    @Bean
    public CircuitBreaker exportCircuitBreaker() {
        return CircuitBreaker.of("export", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .ringBufferSizeInHalfOpenState(10)
            .ringBufferSizeInClosedState(100)
            .build());
    }
}
```

### 8.3 Error Response Mapping
```java
@Component
public class ErrorMapper {
    
    public StoredProcedureResponse mapDatabaseError(DataAccessException e) {
        if (e instanceof SQLTimeoutException) {
            return StoredProcedureResponse.error("DB_TIMEOUT", "Database operation timed out");
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            return StoredProcedureResponse.error("DB_CONSTRAINT", "Data integrity constraint violated");
        } else if (e instanceof BadSqlGrammarException) {
            return StoredProcedureResponse.error("DB_SYNTAX", "SQL syntax error in stored procedure");
        } else {
            return StoredProcedureResponse.error("DB_UNKNOWN", "Unknown database error occurred");
        }
    }
}
```

## 9. Performance Optimization

### 9.1 Query Optimization
- **Stored Procedure Tuning**: Optimize SQL execution plans and indexing
- **Batch Processing**: Batch multiple operations for improved throughput
- **Result Set Streaming**: Stream large result sets to reduce memory usage
- **Query Caching**: Cache frequently accessed query results

### 9.2 Connection Optimization
```java
@Service
public class ConnectionOptimizationService {
    
    @Async("taskExecutor")
    public CompletableFuture<List<ClientDto>> searchClientsAsync(ClientSearchRequest request) {
        return CompletableFuture.completedFuture(searchClients(request));
    }
    
    @EventListener
    public void handleConnectionPoolEvent(ConnectionPoolEvent event) {
        if (event.getEventType() == ConnectionPoolEventType.CONNECTION_ACQUISITION_FAILED) {
            log.warn("Connection pool acquisition failure detected: {}", event);
        }
    }
}
```

### 9.3 Monitoring and Metrics
```java
@Component
public class DatabaseMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Timer.Sample sample;
    
    public void recordStoredProcedureExecution(String procedureName, long durationMs, boolean success) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("database.storedProcedure.execution")
            .tag("procedure", procedureName)
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
    }
    
    public void recordConnectionPoolMetrics(HikariPoolMXBean poolBean) {
 {
        Gauge.builder("database.pool.active")
            .register(meterRegistry, poolBean, p -> p.getActiveConnections());
        Gauge.builder("database.pool.idle")
            .register(meterRegistry, poolBean, p -> p.getIdleConnections());
    }
}
```

## 10. Security Implementation

### 10.1 Internal API Security
```java
@Component
public class InternalSecurityFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Validate required headers
        String userId = httpRequest.getHeader("X-User-ID");
        String advisorId = httpRequest.getHeader("X-Advisor-ID");
        String requestId = httpRequest.getHeader("X-Request-ID");
        String signature = httpRequest.getHeader("X-Signature");
        
        if (!isValidInternalRequest(httpRequest)) {
            throw new SecurityException("Invalid internal request");
        }
        
        // Set security context
        SecurityContext context = InternalSecurityContext.builder()
            .userId(userId)
            .advisorId(advisorId)
            .requestId(requestId)
            .build();
        
        InternalSecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }
}
```

### 10.2 Request Validation
```java
@Component
public class RequestValidator {
    
    public boolean validateInternalRequest(HttpServletRequest request) {
        // Check IP whitelist
        if (!isWhitelistedIp(request.getRemoteAddr())) {
            return false;
        }
        
        // Validate required headers
        String[] requiredHeaders = {"X-User-ID", "X-Advisor-ID", "X-Request-ID"};
        for (String header : requiredHeaders) {
            if (StringUtils.isEmpty(request.getHeader(header))) {
                return false;
            }
        }
        
        // Validate signature
        return validateRequestSignature(request);
    }
    
    private boolean validateRequestSignature(HttpServletRequest request) {
        String payload = request.getReader().lines().collect(Collectors.joining());
        String expectedSignature = calculateSignature(payload, request.getHeader("X-Timestamp"));
        return expectedSignature.equals(request.getHeader("X-Signature"));
    }
}
```

## 11. Asynchronous Processing

### 11.1 Export Job Processing
```java
@Service
public class ExportJobProcessor {
    
    @Async("exportExecutor")
    public CompletableFuture<String> processExportJob(String jobId, ExportRequest request) {
        try {
            // Update job status to PROCESSING
            updateJobStatus(jobId, "PROCESSING", 0);
            
            // Generate export data
            byte[] exportData = generateExportData(request);
            
            // Save to storage and get download URL
            String downloadUrl = saveExportData(jobId, exportData);
            
            // Update job status to COMPLETED
            updateJobStatus(jobId, "COMPLETED", 100, downloadUrl);
            
            return CompletableFuture.completedFuture(downloadUrl);
        } catch (Exception e) {
            updateJobStatus(jobId, "FAILED", 0, null, e.getMessage());
            throw new ExportProcessingException("Export job failed", e);
        }
    }
}
```

### 11.2 Background Task Configuration
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("LFD-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean("exportExecutor")
    public TaskExecutor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("LFD-Export-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
```

## 12. Data Access Layer Integration

### 12.1 Shared Resource Integration
```java
@Service
public class SharedResourceClient {
    
    @Autowired
    private DatabaseServiceClient databaseServiceClient;
    
    public StoredProcedureResponse executeStoredProcedure(String procedureName, Map<String, Object> parameters) {
        try {
            // Map parameters to shared resource DTOs
            StoredProcedureRequest request = StoredProcedureRequest.builder()
                .procedureName(procedureName)
                .parameters(parameters)
                .build();
            
            // Call shared resource service
            return databaseServiceClient.executeProcedure(request);
        } catch (Exception e) {
            log.error("Failed to execute stored procedure: {}", procedureName, e);
            throw new DataAccessException("Stored procedure execution failed", e);
        }
    }
}
```

### 12.2 DTO Mapping
```java
@Component
public class DtoMapper {
    
    public StoredProcedureRequest mapToProcedureRequest(ClientSearchRequest clientRequest) {
        return StoredProcedureRequest.builder()
            .procedureName("sp_search_clients")
            .parameter("p_advisor_id", clientRequest.getAdvisorId())
            .parameter("p_search_query", clientRequest.getSearchQuery())
            .parameter("p_account_types", convertToJson(clientRequest.getAccountTypes()))
            .parameter("p_min_market_value", clientRequest.getMinMarketValue())
            .parameter("p_max_market_value", clientRequest.getMaxMarketValue())
            .parameter("p_page_offset", clientRequest.getPageOffset())
            .parameter("p_page_size", clientRequest.getPageSize())
            .build();
    }
    
    public ClientSearchResponse mapFromProcedureResponse(StoredProcedureResponse response) {
        // Map stored procedure response to internal DTOs
        return ClientSearchResponse.builder()
            .clients(extractClientsFromResponse(response))
            .totalCount(extractTotalCount(response))
            .resultCode(response.getResultCode())
            .errorMessage(response.getErrorMessage())
            .build();
    }
}
```

## 13. Monitoring and Observability

### 13.1 Health Checks
```java
@Component
public class LfdHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            // Test database connectivity
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("connectionPool", getConnectionPoolStatus())
                        .build();
                }
            }
            return Health.down().withDetail("database", "Connection failed").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

### 13.2 Metrics Collection
```java
@Component
public class LfdMetrics {
    
    private final Counter procedureExecutionCounter;
    private final Timer procedureExecutionTimer;
    private final Gauge connectionPoolGauge;
    
    public LfdMetrics(MeterRegistry meterRegistry) {
        this.procedureExecutionCounter = Counter.builder("lfd.procedure.executions")
            .description("Number of stored procedure executions")
            .register(meterRegistry);
        
        this.procedureExecutionTimer = Timer.builder("lfd.procedure.execution.time")
            .description("Time taken to execute stored procedures")
            .register(meterRegistry);
        
        this.connectionPoolGauge = Gauge.builder("lfd.connection.pool.size")
            .description("Current connection pool size")
            .register(meterRegistry, this, LfdMetrics::getConnectionPoolSize);
    }
    
    public void recordProcedureExecution(String procedureName, long durationMs) {
        procedureExecutionCounter.increment(Tags.of("procedure", procedureName));
        procedureExecutionTimer.record(durationMs, TimeUnit.MILLISECONDS, Tags.of("procedure", procedureName));
    }
}
```

## 14. Testing Strategy

### 14.1 Unit Testing
```java
@ExtendWith(MockitoExtension.class)
class LfdServiceTest {
    
    @Mock
    private SharedResourceClient sharedResourceClient;
    
    @InjectMocks
    private LfdService lfdService;
    
    @Test
    void testSearchClients_Success() {
        // Given
        ClientSearchRequest request = createTestRequest();
        StoredProcedureResponse mockResponse = createMockResponse();
        when(sharedResourceClient.executeStoredProcedure(any(), any())).thenReturn(mockResponse);
        
        // When
        ClientSearchResponse response = lfdService.searchClients(request);
        
        // Then
        assertThat(response.getClients()).isNotEmpty();
        assertThat(response.getResultCode()).isEqualTo(0);
        verify(sharedResourceClient).executeStoredProcedure(eq("sp_search_clients"), any());
    }
}
```

### 14.2 Integration Testing
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class LfdIntegrationTest {
    
    @Autowired
    private LfdService lfdService;
    
    @Test
    @Transactional
    void testEndToEndClientSearch() {
        // Setup test data
        setupTestData();
        
        // Execute test
        ClientSearchRequest request = createTestRequest();
        ClientSearchResponse response = lfdService.searchClients(request);
        
        // Verify results
        assertThat(response.getClients()).hasSize(5);
        assertThat(response.getTotalCount()).isEqualTo(5);
    }
}
```

## 15. Deployment Configuration

### 15.1 Application Configuration
```yaml
spring:
  application:
    name: lfd-api-layer
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
server:
  port: 8081
  servlet:
    context-path: /lfd-api

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.bny.lfd: DEBUG
    org.springframework.jdbc: DEBUG
  pattern:
    correlation: "[%correlationId] "
```

### 15.2 Environment-Specific Configurations
```yaml
---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://dev-db.bny.com:3306/investing_dev
    username: ${
    password: ${
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://prod-db.bny.com:3306/investing_prod
    username: ${
    password: ${
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      leak-detection-threshold: 30000
```

## 16. Success Metrics and KPIs

### 16.1 Performance Metrics
- **Stored Procedure Execution Time**: 95th percentile < 500ms
- **Database Connection Pool Utilization**: < 80% average
- **Internal API Response Time**: 95th percentile < 200ms
- **Export Job Processing Time**: < 30 seconds for 1000 holdings

### 16.2 Reliability Metrics
- **Database Operation Success Rate**: > 99.5%
- **Transaction Rollback Rate**: < 0.1%
- **Connection Pool Timeout Rate**: < 0.01%
- **Circuit Breaker Trip Rate**: < 1% per day

### 16.3 Business Metrics
- **Client Search Accuracy**: 100% match with Domain API requirements
- **Holdings Data Freshness**: < 2 minute lag for real-time pricing
- **Export Job Success Rate**: > 99%
- **Audit Log Completeness**: 100% of client access events logged

## 17. Risk Assessment and Mitigation

### 17.1 Technical Risks
- **Database Connection Exhaustion**: Mitigate with connection pooling and circuit breakers
- **Stored Procedure Performance**: Monitor execution times and optimize indexes
- **Transaction Deadlocks**: Implement proper transaction ordering and retry logic
- **Data Consistency**: Use appropriate isolation levels and distributed transactions

### 17.2 Operational Risks
- **Database Failover**: Implement read replica routing and failover procedures
- **Performance Degradation**: Continuous monitoring and alerting on key metrics
- **Security Breaches**: Internal network security and request validation
- **Capacity Planning**: Regular load testing and capacity reviews

## 18. Future Enhancements

### 18.1 Phase 2 Enhancements
- **GraphQL Support**: Internal GraphQL endpoint for complex data queries
- **Event Sourcing**: Implement event-driven architecture for data changes
- **Advanced Caching**: Multi-level caching with Redis and application-level caching
- **Database Sharding**: Horizontal scaling for large datasets

### 18.2 Phase 3 Enhancements
- **Machine Learning Integration**: Predictive analytics for data access patterns
- **Multi-Region Deployment**: Geographic distribution for disaster recovery
- **Real-time Streaming**: Kafka integration for real-time data updates
- **Advanced Monitoring**: AI-powered anomaly detection and alerting

## 19. Acceptance Criteria

### 19.1 Functional Acceptance
1. All internal API endpoints execute successfully with proper stored procedure calls
2. Client search returns accurate results within performance requirements
3. Holdings data includes all calculated fields and performance metrics
4. Export jobs process asynchronously with proper status tracking
5. Market data integration provides both real-time and delayed pricing
6. Authentication and authorization work correctly for internal APIs
7. Audit logging captures all client access events

### 19.2 Non-Functional Acceptance
1. Database operations complete within specified time limits
2. Connection pooling handles peak load without connection exhaustion
3. Error handling and retry mechanisms work as specified
4. Security measures prevent unauthorized internal access
5. Monitoring and alerting provide comprehensive visibility
6. Performance metrics meet or exceed targets
7. Deployment process is automated and reliable

## 20. Appendix

### A. Stored Procedure Summary
| Procedure | Purpose | Input Parameters | Output Parameters |
|-----------|---------|------------------|-------------------|
| sp_search_clients | Complex client search | 12 parameters | 3 output parameters |
| sp_get_advisor_clients | Get advisor's clients | 3 parameters | 3 output parameters |
| sp_get_account_holdings | Retrieve holdings data | 4 parameters | 3 output parameters |
| sp_get_portfolio_summary | Portfolio metrics | 2 parameters | 8 output parameters |
| sp_get_tax_lots | Tax lot information | 3 parameters | 3 output parameters |
| sp_create_export_job | Create export job | 4 parameters | 3 output parameters |
| sp_get_security_prices | Get security prices | 3 parameters | 3 output parameters |

### B. Internal API Endpoints
| Endpoint | Method | Description | Stored Procedure |
|----------|--------|-------------|------------------|
| /internal/clients/search | POST | Search clients with filters | sp_search_clients |
| /internal/advisors/{id}/clients | GET | Get advisor's clients | sp_get_advisor_clients |
| /internal/accounts/{id}/holdings | POST | Get account holdings | sp_get_account_holdings |
| /internal/accounts/{a}/summary | GET | Get portfolio summary | sp_get_portfolio_summary |
| /internal/holdings/{s}/taxlots | GET | Get tax lots | sp_get_tax_lots |
| /internal/export/holdings | POST | Create export job | sp_create_export_job |
| /internal/securities/prices/batch | POST | Get security prices | sp_get_security_prices |

### C. Error Codes
| Code | Description | Action |
|------|-------------|--------|
| LFD_001 | Invalid internal request | Validate headers and signature |
| LFD_002 | Stored procedure execution failed | Check parameters and database connectivity |
| LFD_003 | Transaction timeout | Retry with increased timeout |
| LFD_004 | Connection pool exhausted | Scale up or optimize queries |
| LFD_005 | Export job processing failed | Check job logs and retry |

### D. Configuration Parameters
| Parameter | Default | Description |
|-----------|---------|-------------|
| spring.datasource.hikari.maximum-pool-size | 50 | Maximum connections in pool |
| spring.datasource.hikari.minimum-idle | 10 | Minimum idle connections |
| spring.datasource.hikari.connection-timeout | 20000 | Connection acquisition timeout |
| lfd.circuit-breaker.failure-threshold | 50 | Failure rate threshold for circuit breaker |
| lfd.export.thread-pool-size | 20 | Export processing thread pool size |
| lfd.security.signature-secret | ${SIGNATURE_SECRET} | HMAC signature secret |

### E. Monitoring Metrics
| Metric | Type | Description |
|--------|------|-------------|
| lfd.procedure.executions | Counter | Total stored procedure executions |
| lfd.procedure.execution.time | Timer | Stored procedure execution time |
| lfd.connection.pool.active | Gauge | Active database connections |
| lfd.connection.pool.wait.time | Timer | Connection acquisition wait time |
| lfd.export.jobs.created | Counter | Export jobs created |
| lfd.export.jobs.completed | Counter | Export jobs completed successfully |
