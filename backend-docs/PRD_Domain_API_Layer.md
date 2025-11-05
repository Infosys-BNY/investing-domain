# Product Requirements Document: SpringBoot Domain API Layer

## 1. Executive Summary

### Product Overview
The Domain API Layer is the primary backend service that handles all client-facing API requests for the BNY Advisor Portal. This Spring Boot application implements business logic, validations, authentication, and orchestrates data retrieval from the LFD API Layer.

### Business Objective
Provide a secure, scalable, and high-performance API layer that serves the frontend client selection and holdings view functionality while maintaining strict separation of concerns and adhering to BNY's architectural standards.

### Target Users
- Primary: Frontend Angular Application (BNY Advisor Portal)
- Secondary: Internal BNY services, Third-party integrations (future)

## 2. User Stories

### Primary User Stories
1. **As a frontend application**, I need to search and filter clients, so that advisors can quickly find and select client accounts.
2. **As a frontend application**, I need to retrieve detailed holdings information, so that advisors can view portfolio data with real-time pricing.
3. **As a frontend application**, I need to export holdings data, so that advisors can perform offline analysis and share reports.
4. **As a system administrator**, I need comprehensive audit logging, so that all client data access is tracked for compliance.
5. **As a developer**, I need clear API contracts, so that frontend integration is straightforward and maintainable.

## 3. Functional Requirements

### 3.1 Client Management APIs

#### 3.1.1 Client Search and Retrieval
- `GET /api/v1/advisor/{advisorId}/clients`
  - Retrieve paginated list of clients assigned to advisor
  - Support sorting by name, market value, activity, performance
  - Include account summaries with key metrics
  
- `GET /api/v1/clients/search`
  - Search clients by name, account number, or tax ID (last 4 digits)
  - Auto-complete suggestions after 3 characters
  - Fuzzy matching for name searches
  
- `GET /api/v1/advisor/{advisorId}/recent-clients`
  - Retrieve last 10 accessed clients for quick access
  - Update access timestamp on each retrieval

#### 3.1.2 Client Filtering
- Support multiple filter parameters in single request:
  - Account types (Individual, Joint, IRA, Trust, Corporate)
  - Market value ranges (predefined brackets)
  - Performance filters (positive/negative/neutral)
  - Activity status (Active/Inactive/Dormant)
  - Risk profiles (Conservative/Moderate/Aggressive)
  - Date ranges for last activity

### 3.2 Holdings Management APIs

#### 3.2.1 Holdings Retrieval
- `GET /api/v1/accounts/{accountId}/holdings`
  - Retrieve complete holdings list with all calculated fields
  - Support pagination (default 50, max 1000 per page)
  - Include portfolio summary metrics
  
- `GET /api/v1/accounts/{accountId}/holdings/summary`
  - Retrieve portfolio-level metrics only
  - Cached for 5 minutes to improve performance

#### 3.2.2 Real-time Pricing
- `GET /api/v1/securities/{symbols}/prices`
  - Batch price retrieval for multiple symbols
  - 15-minute delayed pricing for standard users
  - Real-time pricing for premium users
  
- WebSocket endpoint: `ws://api.bny.com/v1/prices/stream`
  - Real-time price updates during market hours
  - Subscribe to specific symbols or entire portfolio
  - Automatic disconnect after 15 minutes of inactivity
  - **Reconnection Strategy**: Exponential backoff with max 30 seconds, 5 retry attempts
  - **Heartbeat**: Ping/Pong every 30 seconds to detect connection issues

#### 3.2.3 Tax Lot Information
- `GET /api/v1/accounts/{accountId}/holdings/{symbol}/taxlots`
  - Retrieve detailed tax lot information for specific position
  - Include purchase dates, quantities, cost basis
  - Calculate holding period (ST/LT) and tax impact

#### 3.2.4 Holdings Filtering and Search
- `GET /api/v1/accounts/{accountId}/holdings/search`
  - Search holdings by symbol or security name
  - Filter by asset class, sector, gain/loss ranges
  - Support complex boolean logic for advanced filtering

### 3.3 Export APIs

#### 3.3.1 Export Functionality
- `POST /api/v1/accounts/{accountId}/holdings/export`
  - Async export job initiation
  - Support Excel, CSV, PDF formats
  - Return job ID for status polling
  
- `GET /api/v1/export/jobs/{jobId}/status`
  - Check export job status
  - Return download URL when complete
  
- `GET /api/v1/export/jobs/{jobId}/download`
  - Download generated export file
  - Secure access with expiration token

### 3.4 Audit and Compliance APIs

#### 3.4.1 Access Logging
- `POST /api/v1/audit/client-access`
  - Log all client data access events
  - Include advisor ID, client ID, timestamp, action type
  
- `GET /api/v1/audit/access-logs`
  - Retrieve audit logs for compliance reporting
  - Admin-only access with role-based authorization

## 4. Data Models and DTOs

### 4.1 Client DTOs
```java
public class ClientDto {
    private String clientId;
    private String clientName;
    private List<AccountDto> accounts;
    private LocalDateTime lastAccessed;
    private String advisorId;
}

public class AccountDto {
    private String accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal marketValue;
    private BigDecimal cashBalance;
    private BigDecimal ytdPerformance;
    private LocalDateTime lastActivity;
    private RiskProfile riskProfile;
}
```

### 4.2 Holdings DTOs
```java
public class HoldingDto {
    private String symbol;
    private String securityName;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private BigDecimal costBasis;
    private BigDecimal totalCost;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercent;
    private BigDecimal portfolioPercent;
    private String sector;
    private AssetClass assetClass;
    private boolean hasAlerts;
    private int taxLotCount;
}

public class PortfolioSummaryDto {
    private BigDecimal totalMarketValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal totalRealizedGainLossYTD;
    private int numberOfHoldings;
    private BigDecimal portfolioBeta;
    private BigDecimal dividendYield;
    private LocalDateTime asOfDate;
}
```

### 4.3 Request/Response Models
```java
public class ClientSearchRequest {
    private String query;
    private List<AccountType> accountTypes;
    private BigDecimal minMarketValue;
    private BigDecimal maxMarketValue;
    private PerformanceFilter performanceFilter;
    private ActivityStatus activityStatus;
    private List<RiskProfile> riskProfiles;
    private LocalDateTime activityStartDate;
    private LocalDateTime activityEndDate;
    private SortField sortField;
    private SortDirection sortDirection;
    private int page = 0;
    private int size = 50;
}

public class PaginatedResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int page;  // Changed from 'number' to match request parameter
    private int size;
    private boolean first;
    private boolean last;
}
```

## 5. Business Logic Implementation

### 5.1 Client Access Control
- **Advisor Assignment Validation**: Verify advisor is assigned to requested clients
- **Role-based Access**: Different data access levels based on advisor role
- **Session Management**: JWT-based authentication with 15-minute timeout
- **Permission Checking**: Validate read/write permissions for each endpoint

### 5.2 Data Calculations
- **Performance Calculations**: YTD performance, unrealized gains/losses
- **Portfolio Metrics**: Beta calculation, dividend yield, concentration analysis
- **Tax Lot Optimization**: FIFO/LIFO/HIFO cost basis methods
- **Risk Assessment**: Portfolio risk metrics and alerts

### 5.3 Caching Strategy
- **Client Lists**: Cache for 5 minutes with advisor-specific keys
- **Holdings Data**: Cache for 2 minutes during market hours, 15 minutes after hours
- **Reference Data**: Security master data cached for 24 hours
- **Tax Lots**: Cache for 1 hour, invalidate on portfolio changes
- **Cache Invalidation Triggers**:
  - Holdings cache invalidated on trade execution, corporate action, or manual refresh
  - Client cache invalidated on advisor assignment changes or client profile updates
  - Reference data cache invalidated daily at 2 AM ET or on security master updates

### 5.4 Error Handling and Validation
- **Input Validation**: Comprehensive request parameter validation
- **Business Rule Validation**: Account access, trading windows, compliance checks
- **Graceful Degradation**: Fallback to cached data when external services fail
- **Standardized Error Responses**: Consistent error format across all endpoints

## 6. API Contracts with LFD Layer

### 6.1 Client Data Calls
```java
// Internal API calls to LFD layer
@LfdClient
public interface LfdClientService {
    
    @GetMapping("/internal/clients/search")
    List<ClientDto> searchClients(ClientSearchCriteria criteria);
    
    @GetMapping("/internal/advisors/{advisorId}/clients")
    PaginatedResponse<ClientDto> getAdvisorClients(
        @PathVariable String advisorId,
        @RequestParam Pageable pageable
    );
    
    @PostMapping("/internal/audit/log-access")
    void logClientAccess(AccessLogEntry logEntry);
}
```

### 6.2 Holdings Data Calls
```java
@LfdClient
public interface LfdHoldingsService {
    
    @GetMapping("/internal/accounts/{accountId}/holdings")
    HoldingsResponseDto getAccountHoldings(
        @PathVariable String accountId,
        @RequestParam(required = false) HoldingFilter filter
    );
    
    @GetMapping("/internal/accounts/{accountId}/summary")
    PortfolioSummaryDto getPortfolioSummary(@PathVariable String accountId);
    
    @GetMapping("/internal/holdings/{symbol}/taxlots")
    List<TaxLotDto> getTaxLots(
        @PathVariable String symbol,
        @PathVariable String accountId
    );
    
    @PostMapping("/internal/export/holdings")
    ExportJobDto initiateExport(ExportRequestDto request);
}
```

## 7. Non-Functional Requirements

### 7.1 Performance Requirements
- **Response Times**:
  - Client search: < 500ms (95th percentile)
  - Holdings retrieval: < 2 seconds for up to 500 holdings
  - Portfolio summary: < 300ms
  - Export initiation: < 1 second
  
- **Throughput**:
  - Support 1000 concurrent users
  - Handle 10,000 requests per minute
  - 99.9% uptime during market hours

### 7.2 Security Requirements
- **Authentication**: JWT tokens with RSA-256 signing
- **Authorization**: Role-based access control (RBAC)
- **Auth Propagation**: Domain API validates JWT, then propagates user context to LFD via trusted headers (X-User-ID, X-Advisor-ID, X-Request-ID)
- **Data Encryption**: TLS 1.3 for all communications
- **Audit Logging**: Complete audit trail for all data access
- **Rate Limiting**: 100 requests per minute per user (enforced at Domain API layer using Redis-based rate limiting)
- **Input Validation**: SQL injection and XSS prevention

### 7.3 Scalability Requirements
- **Horizontal Scaling**: Support multiple instances behind load balancer
- **Database Connection Pooling**: HikariCP with optimal configuration
- **Caching Layer**: Redis cluster for distributed caching
- **Async Processing**: Message queues for export jobs and notifications

### 7.4 Reliability Requirements
- **Circuit Breakers**: Fail fast when downstream services are unavailable
  - LFD API circuit breaker: Open after 5 consecutive failures, 30-second timeout
  - Database circuit breaker: Open after 3 consecutive failures, 60-second timeout
  - Market data circuit breaker: Open after 10 consecutive failures, 15-second timeout
- **Retry Logic**: Exponential backoff for transient failures (max 3 attempts)
- **Health Checks**: Comprehensive health endpoints for monitoring
- **Graceful Shutdown**: Handle in-flight requests during shutdown

## 8. Data Flow Architecture

### 8.1 Request Flow Diagram
```
Frontend → Domain API → LFD API → Shared Resource → Database
    ↑         ↑          ↑             ↑             ↑
  Auth     Business   Database     DTO/Entity    Stored
  Token    Logic      Orchestration Mapping    Procedures
```

### 8.2 Typical Request Sequence
1. **Client Request**: Frontend sends authenticated request
2. **Authentication**: JWT validation and user context extraction
3. **Authorization**: Verify advisor-client relationship
4. **Business Logic**: Apply validation rules and calculations
5. **LFD Call**: Internal API call to data layer
6. **Data Transformation**: Convert LFD response to frontend DTOs
7. **Response**: Return formatted response with audit logging

### 8.3 Real-time Pricing Flow
```
WebSocket Client → Domain API → Price Service → Market Data Feed
       ↓               ↓              ↓              ↓
   Subscription    Message        Cache         External
   Management      Processing     Update        Provider
```

## 9. Error Handling Strategy

### 9.1 Error Categories
- **Validation Errors**: 400 Bad Request with field-level details
- **Authorization Errors**: 403 Forbidden with reason
- **Not Found Errors**: 404 with resource identifier
- **Business Logic Errors**: 422 Unprocessable Entity
- **System Errors**: 500 Internal Server Error with correlation ID

### 9.2 Error Response Format
```json
{
    "timestamp": "2024-10-31T15:45:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid search parameters",
    "details": [
        {
            "field": "marketValueRange",
            "message": "Minimum value cannot be greater than maximum value"
        }
    ],
    "correlationId": "abc123-def456-ghi789",
    "path": "/api/v1/clients/search"
}
```

### 9.3 Exception Handling
- **Global Exception Handler**: Centralized error processing
- **Custom Exceptions**: Domain-specific exception types
- **Logging Integration**: Structured logging with correlation IDs
- **Monitoring Integration**: Error metrics and alerting

## 10. Testing Strategy

### 10.1 Unit Testing
- **Controller Tests**: Test all REST endpoints with mock services
- **Service Tests**: Test business logic with various scenarios
- **Validation Tests**: Test input validation and error handling
- **Coverage Target**: Minimum 80% code coverage

### 10.2 Integration Testing
- **LFD Integration**: Test internal API calls with test containers
- **Database Integration**: Test data access patterns
- **Authentication Tests**: Test JWT validation and authorization
- **End-to-End Tests**: Test complete request flows

### 10.3 Performance Testing
- **Load Testing**: Simulate peak traffic scenarios
- **Stress Testing**: Test system limits and degradation
- **Latency Testing**: Verify response time requirements
- **Concurrency Testing**: Test thread safety and race conditions

## 11. Deployment and Operations

### 11.1 Deployment Architecture
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Kubernetes with auto-scaling
- **Environment Management**: Separate dev, QA, UAT, prod configurations
- **Blue-Green Deployment**: Zero-downtime deployments

### 11.2 Monitoring and Observability
- **Metrics**: Prometheus with custom business metrics
- **Logging**: ELK stack with structured JSON logs
- **Tracing**: OpenTelemetry for distributed tracing
- **Health Checks**: Comprehensive health endpoints
- **Alerting**: PagerDuty integration for critical issues

### 11.3 Configuration Management
- **External Configuration**: Spring Cloud Config
- **Secret Management**: HashiCorp Vault or AWS Secrets Manager
- **Feature Flags**: Togglz for controlled feature rollout
- **Environment Variables**: Sensitive configuration via env vars

## 12. Technology Stack

### 12.1 Core Technologies
- **Framework**: Spring Boot 3.2.x
- **Java Version**: Java 17 LTS
- **Build Tool**: Maven 3.9.x
- **Dependency Management**: Spring Boot Starter BOM

### 12.2 Key Libraries
- **Web**: Spring Web, Spring WebFlux (for WebSocket)
- **Security**: Spring Security, JWT library
- **Validation**: Hibernate Validator
- **Caching**: Spring Cache with Redis
- **Monitoring**: Micrometer, Spring Boot Actuator
- **Testing**: JUnit 5, Mockito, TestContainers

### 12.3 Infrastructure Components
- **Database**: AWS RDS MySQL 8.0
- **Cache**: Redis Cluster
- **Message Queue**: AWS SQS or RabbitMQ
- **Load Balancer**: AWS ALB
- **Container Registry**: AWS ECR

## 13. Success Metrics and KPIs

### 13.1 Performance Metrics
- **API Response Time**: 95th percentile < 2 seconds
- **Error Rate**: < 0.1% of total requests
- **Availability**: 99.9% uptime during market hours
- **Throughput**: Support 10,000 requests/minute

### 13.2 Business Metrics
- **Client Search Success Rate**: > 95%
- **Holdings Data Accuracy**: 99.9% match with source systems
- **Export Success Rate**: > 99%
- **User Satisfaction**: > 4.5/5 rating

### 13.3 Operational Metrics
- **Mean Time to Recovery (MTTR)**: < 15 minutes
- **Deployment Frequency**: Weekly releases
- **Change Failure Rate**: < 5%
- **Security Incident Rate**: Zero critical incidents

## 14. Dependencies and Integration Points

### 14.1 Upstream Dependencies
- **Frontend Application**: Angular-based BNY Advisor Portal
- **Authentication Service**: BNY SSO/Identity Provider
- **Authorization Service**: Role-based access control system

### 14.2 Downstream Dependencies
- **LFD API Layer**: Internal data access layer
- **Market Data Service**: Real-time pricing feeds
- **Notification Service**: Email and alert delivery
- **Audit Service**: Centralized audit logging

### 14.3 External Dependencies
- **Market Data Providers**: Real-time and delayed pricing
- **Reference Data Services**: Security master data
- **Compliance Services**: Regulatory rule validation

## 15. Risk Assessment and Mitigation

### 15.1 Technical Risks
- **Performance Bottlenecks**: Mitigate with caching and async processing
- **Data Consistency**: Implement eventual consistency patterns
- **Security Vulnerabilities**: Regular security audits and penetration testing
- **Service Dependencies**: Circuit breakers and fallback mechanisms

### 15.2 Business Risks
- **Data Accuracy**: Implement comprehensive validation and reconciliation
- **Regulatory Compliance**: Ensure all requirements are met and auditable
- **User Adoption**: Provide comprehensive documentation and training
- **Scalability Limits**: Design for horizontal scaling from day one

## 16. Future Enhancements and Roadmap

### 16.1 Phase 2 Features (6-12 months)
- **Advanced Analytics**: Portfolio analytics and risk metrics
- **AI-Powered Insights**: Automated recommendations and alerts
- **Mobile API Support**: Optimized endpoints for mobile applications
- **Batch Processing**: Large-scale data processing capabilities

### 16.2 Phase 3 Features (12-18 months)
- **GraphQL Support**: Flexible data querying capabilities
- **Event-Driven Architecture**: Kafka integration for real-time events
- **Multi-tenancy**: Support for multiple business units
- **Internationalization**: Multi-language and multi-currency support

## 17. Acceptance Criteria

### 17.1 Functional Acceptance
1. All frontend API requirements are implemented and tested
2. Client search returns accurate results within performance requirements
3. Holdings data displays correctly with all calculated fields
4. Export functionality generates accurate files in all formats
5. Real-time pricing updates work during market hours
6. Authentication and authorization work as specified
7. All error scenarios are handled gracefully

### 17.2 Non-Functional Acceptance
1. Performance requirements are met under load testing
2. Security requirements are validated through penetration testing
3. Availability targets are achieved in production
4. Monitoring and alerting are fully operational
5. Documentation is complete and accurate
6. Deployment process is automated and reliable

## 19. API Versioning Strategy

### 19.1 Versioning Approach
- **URL-based versioning**: Use `/api/v1/`, `/api/v2/` pattern
- **Version Support**: Maintain backward compatibility for 2 previous major versions
- **Deprecation Policy**: 6-month deprecation notice for breaking changes
- **Version Detection**: Automatic version detection from URL, fallback to latest stable

### 19.2 Version Evolution
- **v1**: Current version with basic client and holdings functionality
- **v2**: Planned additions for advanced analytics and GraphQL support
- **Breaking Changes**: Require new major version, documented in changelog
- **Non-breaking Changes**: Additive changes within same version

### 20. Rate Limiting Implementation

### 20.1 Rate Limiting Strategy
- **Redis-based**: Distributed rate limiting using Redis sliding window
- **User-scoped**: Limits applied per authenticated user ID
- **Endpoint-specific**: Different limits for different endpoint types
- **Burst Handling**: Allow short bursts with token bucket algorithm

### 20.2 Rate Limiting Configuration
```yaml
rate-limiting:
  default:
    requests-per-minute: 100
    burst-capacity: 20
  search-endpoints:
    requests-per-minute: 200
    burst-capacity: 50
  export-endpoints:
    requests-per-minute: 10
    burst-capacity: 5
  websocket:
    connections-per-user: 5
    messages-per-second: 100
```

## 21. Pagination Convention

### 21.1 Standardized Pagination
- **Request Parameters**: `page` (0-based), `size` (default 50, max 1000)
- **Response Format**: Consistent `PaginatedResponse<T>` with `number` and `size`
- **Sorting**: `sort` parameter with field and direction (e.g., `clientName,asc`)
- **Navigation**: Include first/last/next/previous page links in response headers

### 22. Appendix
| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| /api/v1/advisor/{id}/clients | GET | Get advisor's clients | Yes |
| /api/v1/clients/search | GET | Search clients | Yes |
| /api/v1/accounts/{id}/holdings | GET | Get account holdings | Yes |
| /api/v1/accounts/{id}/holdings/export | POST | Export holdings | Yes |
| /ws/v1/prices/stream | WS | Real-time prices | Yes |

### B. Error Code Reference
| Code | Description | Action Required |
|------|-------------|-----------------|
| AUTH_001 | Invalid JWT token | Re-authenticate |
| AUTH_002 | Insufficient permissions | Contact admin |
| BIZ_001 | Client not assigned to advisor | Verify assignment |
| BIZ_002 | Account not found | Verify account ID |
| SYS_001 | Database connection failed | Retry or contact support |

### C. Configuration Parameters
| Parameter | Default | Description |
|-----------|---------|-------------|
| server.port | 8080 | Application port |
| cache.ttl.clients | 300s | Client cache TTL |
| cache.ttl.holdings | 120s | Holdings cache TTL |
| ratelimit.requests-per-minute | 100 | Rate limit per user |
| websocket.timeout | 900s | WebSocket timeout |

### D. Monitoring Metrics
| Metric | Type | Description |
|--------|------|-------------|
| api.request.count | Counter | Total API requests |
| api.request.duration | Timer | Request duration |
| api.error.count | Counter | Error count by type |
| cache.hit.rate | Gauge | Cache hit percentage |
| database.connection.pool | Gauge | DB connection pool usage |
