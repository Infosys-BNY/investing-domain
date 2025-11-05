# Product Requirements Document: Data Services Shared Resource

## 1. Executive Summary

### Product Overview
The Data Services Shared Resource is a common Spring Boot library that provides database abstraction, DTO definitions, and utility services used by both the Domain API Layer and LFD API Layer. This shared resource encapsulates database access patterns, stored procedure execution, and common data models to ensure consistency across the BNY Advisor Portal backend architecture.

### Business Objective
Provide a reusable, maintainable, and efficient data access layer that abstracts database complexity, standardizes data models, and enables seamless integration between the Domain API and LFD API layers while promoting code reuse and consistency.

### Target Users
- Primary: Domain API Layer (internal consumer)
- Primary: LFD API Layer (internal consumer)
- Secondary: Future backend services requiring database access

## 2. User Stories

### Primary User Stories
1. **As the LFD API Layer**, I need stored procedure execution utilities, so that I can efficiently call database procedures with proper parameter mapping.
2. **As the Domain API Layer**, I need common DTO definitions, so that I can maintain data consistency across layers.
3. **As a developer**, I need database connection management, so that I can handle connections efficiently without boilerplate code.
4. **As a system administrator**, I need comprehensive database monitoring, so that I can track performance and identify issues.
5. **As a developer**, I need result set mapping utilities, so that I can convert database results to Java objects seamlessly.

## 3. Functional Requirements

### 3.1 Database Access Components

#### 3.1.1 Stored Procedure Execution Service
- **Procedure Executor**: Generic stored procedure execution with parameter binding
- **Parameter Mapping**: Automatic mapping of Java objects to SQL parameters
- **Result Set Processing**: Efficient conversion of SQL results to Java objects
- **Output Parameter Handling**: Proper extraction of OUT parameters from stored procedures
- **Transaction Integration**: Seamless integration with Spring transaction management

#### 3.1.2 JDBC Template Wrappers
- **Enhanced JdbcTemplate**: Extended JDBC template with additional convenience methods
- **Batch Operations**: Optimized batch execution for large datasets
- **Connection Management**: Automatic connection handling and cleanup
- **Error Mapping**: Comprehensive SQL exception mapping to business exceptions

#### 3.1.3 Database Connection Utilities
- **Connection Pool Management**: HikariCP integration and monitoring
- **Multi-Database Support**: Support for different database environments
- **Failover Handling**: Automatic failover to read replicas for read operations
- **Connection Validation**: Pre-execution connection health checks

### 3.2 Common Data Models

#### 3.2.1 Entity Definitions
- **Client Entities**: Client and account related database entities
- **Holdings Entities**: Portfolio holdings and position entities
- **Security Entities**: Security master and price data entities
- **Audit Entities**: Audit logging and tracking entities
- **Export Entities**: Export job and status tracking entities

#### 3.2.2 DTO Definitions
- **Request DTOs**: Standardized request data transfer objects
- **Response DTOs**: Standardized response data transfer objects
- **Parameter DTOs**: Stored procedure parameter objects
- **Result DTOs**: Stored procedure result objects

#### 3.2.3 Enumerations and Constants
- **Account Types**: Standardized account type enumerations
- **Asset Classes**: Asset class and sector classifications
- **Status Codes**: Standardized status and error codes
- **Database Constants**: Database-specific constants and configurations

### 3.4 Batch Operation Support

#### 3.4.1 Batch Insert/Update Utilities
- **Batch Insert Service**: Optimized batch insertion for large datasets
- **Batch Update Service**: Efficient batch updates with transaction management
- **Bulk Export Support**: Batch processing for export job data generation
- **Performance Monitoring**: Track batch operation performance and resource usage

#### 3.4.2 Batch Processing Configuration
```java
@Service
public class BatchOperationService {
    
    private final JdbcTemplate jdbcTemplate;
    
    public int[] batchInsert(String sql, List<Object[]> batchArgs) {
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }
    
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }
    
    public void executeBatchInTransaction(List<BatchOperation> operations) {
        TransactionTemplate transactionTemplate = getTransactionTemplate();
        transactionTemplate.execute(status -> {
            try {
                for (BatchOperation operation : operations) {
                    operation.execute();
                }
                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new BatchOperationException("Batch operation failed", e);
            }
        });
    }
}
```

### 3.5 Usage Guidelines

#### 3.5.1 Entity vs DTO Usage
- **Entities**: Use within the shared resource layer for database operations and JPA/Hibernate interactions
- **DTOs**: Use for data transfer between layers (Domain API ↔ LFD API ↔ Shared Resource)
- **Parameter Objects**: Use for stored procedure parameter passing
- **Result Objects**: Use for stored procedure result handling

#### 3.5.2 Best Practices
- Always use DTOs for inter-layer communication
- Use entities only within the shared resource layer for database operations
- Validate all input parameters before database operations
- Use batch operations for large datasets (> 100 records)
- Implement proper exception handling for all database operations

### 3.3 Utility Services

#### 3.3.1 Data Validation Utilities
- **Input Validation**: Common validation rules for database inputs
- **Business Rule Validation**: Shared business logic validation
- **Data Format Validation**: Date, number, and string format validation
- **Security Validation**: SQL injection prevention and data sanitization

#### 3.3.2 Data Transformation Utilities
- **JSON Mapping**: JSON to Java object conversion utilities
- **Date/Time Handling**: Standardized date and time formatting
- **Currency Formatting**: Financial data formatting and rounding
- **String Manipulation**: Common string processing utilities

#### 3.3.3 Error Handling Utilities
- **Exception Mapping**: Database exception to business exception mapping
- **Error Code Generation**: Standardized error code generation
- **Logging Utilities**: Structured logging with correlation IDs
- **Monitoring Integration**: Metrics collection for database operations

## 4. Core Components Architecture

### 4.1 Database Service Layer
```java
@Service
public class DatabaseService {
    
    private final JdbcTemplate jdbcTemplate;
    private final StoredProcedureExecutor storedProcedureExecutor;
    
    public StoredProcedureResponse executeProcedure(String procedureName, Map<String, Object> parameters) {
        return storedProcedureExecutor.execute(procedureName, parameters);
    }
    
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return jdbcTemplate.query(sql, rowMapper, args);
    }
    
    public int update(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }
    
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return jdbcTemplate.queryForObject(sql, rowMapper, args);
    }
}
```

### 4.2 Stored Procedure Executor
```java
@Component
public class StoredProcedureExecutor {
    
    private final JdbcTemplate jdbcTemplate;
    private final ParameterMapper parameterMapper;
    private final ResultSetMapper resultSetMapper;
    
    public StoredProcedureResponse execute(String procedureName, Map<String, Object> parameters) {
        try {
            // Build stored procedure call
            String procedureCall = buildProcedureCall(procedureName, parameters);
            
            // Execute stored procedure
            Map<String, Object> result = jdbcTemplate.call(
                new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(procedureName)
                    .declareParameters(buildParameters(parameters)),
                extractInputParameters(parameters)
            );
            
            // Map results
            return StoredProcedureResponse.builder()
                .resultCode(extractResultCode(result))
                .errorMessage(extractErrorMessage(result))
                .data(extractData(result))
                .outputParameters(extractOutputParameters(result))
                .build();
                
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to execute stored procedure: " + procedureName, e);
        }
    }
    
    private Map<String, Object> extractOutputParameters(Map<String, Object> result) {
        Map<String, Object> outputParams = new HashMap<>();
        result.forEach((key, value) -> {
            if (key.startsWith("p_") && isOutputParameter(key)) {
                outputParams.put(key, value);
            }
        });
        return outputParams;
    }
    
    private int extractResultCode(Map<String, Object> result) {
        Object resultCode = result.get("p_result_code");
        return resultCode != null ? (Integer) resultCode : -1;
    }
    
    private String extractErrorMessage(Map<String, Object> result) {
        Object errorMessage = result.get("p_error_message");
        return errorMessage != null ? (String) errorMessage : "Unknown error";
    }
    
    private Object extractData(Map<String, Object> result) {
        // Extract result set data (typically under "#result-set-1" key)
        return result.get("#result-set-1");
    }
    
    private String buildProcedureCall(String procedureName, Map<String, Object> parameters) {
        StringBuilder call = new StringBuilder("{call ");
        call.append(procedureName).append("(");
        
        parameters.keySet().forEach(param -> call.append("?,"));
        if (!parameters.isEmpty()) {
            call.setLength(call.length() - 1); // Remove trailing comma
        }
        
        call.append(")}");
        return call.toString();
    }
    
    private boolean isOutputParameter(String parameterName) {
        return parameterName.endsWith("_code") || 
               parameterName.endsWith("_message") || 
               parameterName.endsWith("_count") ||
               parameterName.endsWith("_id");
    }
}
```

### 4.3 Parameter Mapper
```java
@Component
public class ParameterMapper {
    
    public SqlParameter[] buildParameters(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
            .map(entry -> createSqlParameter(entry.getKey(), entry.getValue()))
            .toArray(SqlParameter[]::new);
    }
    
    private SqlParameter createSqlParameter(String name, Object value) {
        if (name.startsWith("p_") && isOutputParameter(name)) {
            return new SqlOutParameter(name, getSqlType(value));
        } else {
            return new SqlParameter(name, getSqlType(value));
        }
    }
    
    private int getSqlType(Object value) {
        if (value instanceof String) return Types.VARCHAR;
        if (value instanceof Integer) return Types.INTEGER;
        if (value instanceof Long) return Types.BIGINT;
        if (value instanceof BigDecimal) return Types.DECIMAL;
        if (value instanceof LocalDateTime) return Types.TIMESTAMP;
        if (value instanceof Boolean) return Types.BOOLEAN;
        if (value instanceof JSON) return Types.JSON;
        return Types.VARCHAR; // Default
    }
    
    private boolean isOutputParameter(String parameterName) {
        return parameterName.endsWith("_code") || 
               parameterName.endsWith("_message") || 
               parameterName.endsWith("_count") ||
               parameterName.endsWith("_id");
    }
}
```

### 4.4 Result Set Mapper
```java
@Component
public class ResultSetMapper {
    
    public List<ClientDto> mapToClients(ResultSet rs) throws SQLException {
        List<ClientDto> clients = new ArrayList<>();
        while (rs.next()) {
            clients.add(ClientDto.builder()
                .clientId(rs.getString("client_id"))
                .clientName(rs.getString("client_name"))
                .advisorId(rs.getString("advisor_id"))
                .lastAccessed(rs.getTimestamp("last_accessed").toLocalDateTime())
                .build());
        }
        return clients;
    }
    
    public List<HoldingDto> mapToHoldings(ResultSet rs) throws SQLException {
        List<HoldingDto> holdings = new ArrayList<>();
        while (rs.next()) {
            holdings.add(HoldingDto.builder()
                .symbol(rs.getString("symbol"))
                .securityName(rs.getString("security_name"))
                .quantity(rs.getBigDecimal("quantity"))
                .currentPrice(rs.getBigDecimal("current_price"))
                .costBasis(rs.getBigDecimal("cost_basis"))
                .marketValue(rs.getBigDecimal("market_value"))
                .unrealizedGainLoss(rs.getBigDecimal("unrealized_gain_loss"))
                .unrealizedGainLossPercent(rs.getBigDecimal("unrealized_gain_loss_percent"))
                .portfolioPercent(rs.getBigDecimal("portfolio_percent"))
                .sector(rs.getString("sector"))
                .assetClass(AssetClass.valueOf(rs.getString("asset_class")))
                .build());
        }
        return holdings;
    }
    
    public PortfolioSummaryDto mapToPortfolioSummary(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return PortfolioSummaryDto.builder()
                .totalMarketValue(rs.getBigDecimal("total_market_value"))
                .totalCostBasis(rs.getBigDecimal("total_cost_basis"))
                .totalUnrealizedGainLoss(rs.getBigDecimal("total_unrealized_gain_loss"))
                .totalRealizedGainLossYTD(rs.getBigDecimal("total_realized_gain_loss_ytd"))
                .numberOfHoldings(rs.getInt("number_of_holdings"))
                .portfolioBeta(rs.getBigDecimal("portfolio_beta"))
                .dividendYield(rs.getBigDecimal("dividend_yield"))
                .asOfDate(rs.getTimestamp("as_of_date").toLocalDateTime())
                .build();
        }
        return null;
    }
}
```

## 5. Data Models and DTOs

### 5.1 Client Data Models
```java
@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "client_name")
    private String clientName;
    
    @Column(name = "advisor_id")
    private String advisorId;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @OneToMany(mappedBy = "clientId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountEntity> accounts;
    
    @OneToMany(mappedBy = "clientId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLogEntity> auditLogs;
    
    // Getters and setters
}

@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    @Column(name = "account_id")
    private String accountId;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @Column(name = "market_value")
    private BigDecimal marketValue;
    
    @Column(name = "cash_balance")
    private BigDecimal cashBalance;
    
    @Column(name = "ytd_performance")
    private BigDecimal ytdPerformance;
    
    @Column(name = "risk_profile")
    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private ClientEntity client;
    
    @OneToMany(mappedBy = "accountId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoldingEntity> holdings;
    
    @OneToMany(mappedBy = "accountId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLogEntity> accessLogs;
    
    // Getters and setters
}
```

### 5.2 Holdings Data Models
```java
@Entity
@Table(name = "holdings")
public class HoldingEntity {
    @Id
    @Column(name = "holding_id")
    private String holdingId;
    
    @Column(name = "account_id")
    private String accountId;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "quantity")
    private BigDecimal quantity;
    
    @Column(name = "cost_basis")
    private BigDecimal costBasis;
    
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", insertable = false, updatable = false)
    private SecurityEntity security;
    
    @OneToMany(mappedBy = "holdingId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaxLotEntity> taxLots;
    
    // Getters and setters
}

@Entity
@Table(name = "securities")
public class SecurityEntity {
    @Id
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "security_name")
    private String securityName;
    
    @Column(name = "sector")
    private String sector;
    
    @Column(name = "asset_class")
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;
    
    @Column(name = "current_price")
    private BigDecimal currentPrice;
    
    @Column(name = "price_change")
    private BigDecimal priceChange;
    
    @Column(name = "price_change_percent")
    private BigDecimal priceChangePercent;
    
    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;
    
    @OneToMany(mappedBy = "symbol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoldingEntity> holdings;
    
    // Getters and setters
}

@Entity
@Table(name = "tax_lots")
public class TaxLotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_lot_id")
    private Long taxLotId;
    
    @Column(name = "holding_id")
    private String holdingId;
    
    @Column(name = "lot_number")
    private Integer lotNumber;
    
    @Column(name = "quantity")
    private BigDecimal quantity;
    
    @Column(name = "cost_basis")
    private BigDecimal costBasis;
    
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column(name = "holding_period")
    @Enumerated(EnumType.STRING)
    private HoldingPeriod holdingPeriod;
    
    @Column(name = "tax_impact_estimate")
    private BigDecimal taxImpactEstimate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", insertable = false, updatable = false)
    private HoldingEntity holding;
    
    // Getters and setters
}

public enum HoldingPeriod {
    SHORT_TERM("Short-term"),
    LONG_TERM("Long-term");
    
    private final String displayName;
    
    HoldingPeriod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### 5.3 Audit and Tracking Entities
```java
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long auditLogId;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "action_type")
    private String actionType;
    
    @Column(name = "action_details")
    private String actionDetails;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "request_id")
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private ClientEntity client;
    
    // Getters and setters
}

@Entity
@Table(name = "access_logs")
public class AccessLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_log_id")
    private Long accessLogId;
    
    @Column(name = "account_id")
    private String accountId;
    
    @Column(name = "advisor_id")
    private String advisorId;
    
    @Column(name = "access_type")
    private String accessType;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "request_id")
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    // Getters and setters
}

@Entity
@Table(name = "export_jobs")
public class ExportJobEntity {
    @Id
    @Column(name = "job_id")
    private String jobId;
    
    @Column(name = "account_id")
    private String accountId;
    
    @Column(name = "export_format")
    @Enumerated(EnumType.STRING)
    private ExportFormat exportFormat;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ExportStatus status;
    
    @Column(name = "filter_criteria")
    private String filterCriteria;
    
    @Column(name = "requested_by")
    private String requestedBy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "progress_percent")
    private Integer progressPercent;
    
    @Column(name = "download_url")
    private String downloadUrl;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    // Getters and setters
}

public enum ExportFormat {
    EXCEL("Excel"),
    CSV("CSV"),
    PDF("PDF");
    
    private final String displayName;
    
    ExportFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

### 5.4 Common DTOs
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedureResponse {
    private int resultCode;
    private String errorMessage;
    private Object data;
    private Map<String, Object> outputParameters;
    
    public static StoredProcedureResponse success(Object data) {
        return StoredProcedureResponse.builder()
            .resultCode(0)
            .data(data)
            .build();
    }
    
    public static StoredProcedureResponse error(int code, String message) {
        return StoredProcedureResponse.builder()
            .resultCode(code)
            .errorMessage(message)
            .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedureRequest {
    private String procedureName;
    private Map<String, Object> parameters;
    private Map<String, Object> outputParameters;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseOperationResult {
    private boolean success;
    private int affectedRows;
    private Object resultData;
    private String errorCode;
    private String errorMessage;
    private long executionTimeMs;
}
```

### 5.4 Enumerations
```java
public enum AccountType {
    INDIVIDUAL("Individual"),
    JOINT("Joint"),
    IRA("IRA"),
    TRUST("Trust"),
    CORPORATE("Corporate"),
    UMA("Unified Managed Account");
    
    private final String displayName;
    
    AccountType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

public enum AssetClass {
    EQUITY("Equity"),
    FIXED_INCOME("Fixed Income"),
    ALTERNATIVE("Alternative"),
    CASH("Cash"),
    DERIVATIVE("Derivative");
    
    private final String displayName;
    
    AssetClass(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

public enum RiskProfile {
    CONSERVATIVE("Conservative"),
    MODERATE("Moderate"),
    AGGRESSIVE("Aggressive");
    
    private final String displayName;
    
    RiskProfile(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

public enum ExportStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    ExportStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

## 6. Database Configuration

### 6.1 DataSource Configuration
```java
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    
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
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

### 6.2 HikariCP Configuration
```yaml
spring:
  datasource:
    primary:
      type: com.zaxxer.hikari.HikariDataSource
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: SharedResource-Primary-Pool
        maximum-pool-size: 20
        minimum-idle: 5
        idle-timeout: 300000
        max-lifetime: 1800000
        connection-timeout: 20000
        leak-detection-threshold: 60000
        connection-test-query: SELECT 1
        validation-timeout: 3000
    readonly:
      type: com.zaxxer.hikari.HikariDataSource
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: SharedResource-ReadOnly-Pool
        maximum-pool-size: 15
        minimum-idle: 3
        idle-timeout: 300000
        max-lifetime: 1800000
        connection-timeout: 20000
        leak-detection-threshold: 60000
        connection-test-query: SELECT 1
        validation-timeout: 3000
```

### 6.3 Transaction Configuration
```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setTimeout(30); // 30 seconds
        return template;
    }
    
    @Bean
    public TransactionAttributeSource transactionAttributeSource() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        
        RuleBasedTransactionAttribute readTx = new RuleBasedTransactionAttribute();
        readTx.setReadOnly(true);
        readTx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        
        RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
        writeTx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeTx.setRollbackFor(Exception.class);
        
        RuleBasedTransactionAttribute criticalTx = new RuleBasedTransactionAttribute();
        criticalTx.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        criticalTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        criticalTx.setRollbackFor(Exception.class);
        
        source.addTransactionalMethod("query*", readTx);
        source.addTransactionalMethod("get*", readTx);
        source.addTransactionalMethod("find*", readTx);
        source.addTransactionalMethod("execute*", writeTx);
        source.addTransactionalMethod("update*", writeTx);
        source.addTransactionalMethod("insert*", writeTx);
        source.addTransactionalMethod("delete*", writeTx);
        source.addTransactionalMethod("log*", criticalTx);
        
        return source;
    }
}
```

## 7. Validation and Error Handling

### 7.1 Input Validation
```java
@Component
public class DatabaseInputValidator {
    
    public void validateProcedureParameters(String procedureName, Map<String, Object> parameters) {
        if (StringUtils.isEmpty(procedureName)) {
            throw new IllegalArgumentException("Procedure name cannot be null or empty");
        }
        
        if (parameters == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        parameters.forEach((key, value) -> {
            if (StringUtils.isEmpty(key)) {
                throw new IllegalArgumentException("Parameter key cannot be null or empty");
            }
            validateParameterValue(key, value);
        });
    }
    
    private void validateParameterValue(String key, Object value) {
        if (value == null) {
            return; // Null values are allowed for optional parameters
        }
        
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.length() > 1000) {
                throw new IllegalArgumentException("String parameter '" + key + "' exceeds maximum length");
            }
            if (containsSqlInjection(stringValue)) {
                throw new SecurityException("Potential SQL injection detected in parameter '" + key + "'");
            }
        }
        
        if (value instanceof BigDecimal) {
            BigDecimal decimalValue = (BigDecimal) value;
            if (decimalValue.scale() > 6) {
                throw new IllegalArgumentException("Decimal parameter '" + key + "' exceeds maximum scale");
            }
        }
    }
    
    private boolean containsSqlInjection(String input) {
        String[] suspiciousPatterns = {
            "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_", "DROP", "DELETE", "INSERT", "UPDATE"
        };
        
        String upperInput = input.toUpperCase();
        for (String pattern : suspiciousPatterns) {
            if (upperInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
```

### 7.2 Exception Mapping
```java
@Component
public class DatabaseExceptionMapper {
    
    public DatabaseOperationException mapException(DataAccessException e) {
        if (e instanceof SQLSyntaxErrorException) {
            return new DatabaseOperationException("SQL syntax error", "DB_SYNTAX_ERROR", e);
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            return new DatabaseOperationException("Data integrity constraint violation", "DB_CONSTRAINT_VIOLATION", e);
        } else if (e instanceof SQLTimeoutException) {
            return new DatabaseOperationException("Database operation timeout", "DB_TIMEOUT", e);
        } else if (e instanceof SQLTransientConnectionException) {
            return new DatabaseOperationException("Transient database connection error", "DB_CONNECTION_ERROR", e);
        } else if (e instanceof DataAccessResourceFailureException) {
            return new DatabaseOperationException("Database resource failure", "DB_RESOURCE_FAILURE", e);
        } else {
            return new DatabaseOperationException("Unknown database error", "DB_UNKNOWN_ERROR", e);
        }
    }
    
    public StoredProcedureResponse mapToErrorResponse(DataAccessException e) {
        DatabaseOperationException mappedException = mapException(e);
        return StoredProcedureResponse.error(
            Integer.parseInt(mappedException.getErrorCode()),
            mappedException.getMessage()
        );
    }
}

@Data
@AllArgsConstructor
public class DatabaseOperationException extends RuntimeException {
    private String message;
    private String errorCode;
    private Throwable cause;
}
```

## 8. Performance Optimization

### 8.1 Connection Pool Optimization
```java
@Component
public class ConnectionPoolOptimizer {
    
    @EventListener(ApplicationReadyEvent.class)
    public void optimizeConnectionPools() {
        // Monitor and optimize connection pools
        monitorPoolMetrics();
        configurePoolSettings();
    }
    
    private void monitorPoolMetrics() {
        // Implement pool monitoring logic
        // Track active connections, idle connections, wait times
    }
    
    private void configurePoolSettings() {
        // Dynamic pool configuration based on workload
        // Adjust pool sizes based on current load
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void reportPoolMetrics() {
        // Report pool metrics to monitoring system
        // Alert on pool exhaustion or high wait times
    }
}
```

### 8.2 Query Optimization
```java
@Component
public class QueryOptimizer {
    
    private final JdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;
    
    public <T> List<T> executeOptimizedQuery(String sql, RowMapper<T> rowMapper, Object... args) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<T> result = jdbcTemplate.query(sql, rowMapper, args);
            sample.stop(Timer.builder("database.query.execution")
                .tag("query", extractQueryName(sql))
                .register(meterRegistry));
            return result;
        } catch (DataAccessException e) {
            sample.stop(Timer.builder("database.query.execution")
                .tag("query", extractQueryName(sql))
                .tag("success", "false")
                .register(meterRegistry));
            throw e;
        }
    }
    
    private String extractQueryName(String sql) {
        // Extract meaningful query name from SQL
        if (sql.contains("sp_search_clients")) return "search_clients";
        if (sql.contains("sp_get_account_holdings")) return "get_holdings";
        if (sql.contains("sp_get_portfolio_summary")) return "portfolio_summary";
        return "unknown";
    }
}
```

### 8.3 Caching Integration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
            new RedisStandaloneConfiguration("localhost", 6379));
    }
}

@Component
public class DatabaseCacheManager {
    
    private final CacheManager cacheManager;
    
    @Cacheable(value = "clients", key = "#advisorId", unless = "#result == null")
    public List<ClientDto> getCachedClients(String advisorId) {
        // Implementation will be provided by calling layers
        return null;
    }
    
    @CacheEvict(value = "clients", key = "#advisorId")
    public void evictClientCache(String advisorId) {
        // Cache eviction logic
    }
    
    @Cacheable(value = "holdings", key = "#accountId", unless = "#result == null")
    public List<HoldingDto> getCachedHoldings(String accountId) {
        // Implementation will be provided by calling layers
        return null;
    }
    
    @CacheEvict(value = "holdings", key = "#accountId")
    public void evictHoldingsCache(String accountId) {
        // Cache eviction logic
    }
    
    @Cacheable(value = "taxlots", key = "#accountId + ':' + #symbol", unless = "#result == null")
    public List<TaxLotDto> getCachedTaxLots(String accountId, String symbol) {
        // Implementation will be provided by calling layers
        return null;
    }
    
    @CacheEvict(value = "taxlots", key = "#accountId + ':' + #symbol")
    public void evictTaxLotCache(String accountId, String symbol) {
        // Cache eviction logic
    }
}
```

## 9. Monitoring and Metrics

### 9.1 Database Metrics
```java
@Component
public class DatabaseMetrics {
    
    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseMetrics(MeterRegistry meterRegistry, JdbcTemplate jdbcTemplate) {
        this.meterRegistry = meterRegistry;
        this.jdbcTemplate = jdbcTemplate;
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        // Initialize database connection metrics
        Gauge.builder("database.connections.active")
            .register(meterRegistry, this, DatabaseMetrics::getActiveConnections);
        
        Gauge.builder("database.connections.idle")
            .register(meterRegistry, this, DatabaseMetrics::getIdleConnections);
        
        Counter.builder("database.queries.executed")
            .register(meterRegistry);
        
        Timer.builder("database.queries.duration")
            .register(meterRegistry);
    }
    
    private double getActiveConnections() {
        // Implementation to get active connection count
        return 0.0;
    }
    
    private double getIdleConnections() {
        // Implementation to get idle connection count
        return 0.0;
    }
    
    public void recordQueryExecution(String queryType, long durationMs, boolean success) {
        Counter.builder("database.queries.executed")
            .tag("type", queryType)
            .tag("success", String.valueOf(success))
            .increment(meterRegistry);
        
        Timer.builder("database.queries.duration")
            .tag("type", queryType)
            .record(durationMs, TimeUnit.MILLISECONDS, meterRegistry);
    }
}
```

### 9.2 Health Monitoring
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Health health() {
        try {
            // Test database connectivity
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result != null && result == 1) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("testQuery", "SELECT 1")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Unexpected test result")
                    .build();
            }
        } catch (DataAccessException e) {
            return Health.down(e)
                .withDetail("database", "Connection failed")
                .build();
        }
    }
}
```

## 10. Testing Framework

### 10.1 Test Configuration
```java
@TestConfiguration
public class DatabaseTestConfig {
    
    @Bean
    @Primary
    public DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("schema-test.sql")
            .addScript("data-test.sql")
            .build();
    }
    
    @Bean
    public TestEntityManager testEntityManager(DataSource dataSource) {
        return new TestEntityManager(dataSource);
    }
}
```

### 10.2 Test Utilities
```java
@Component
public class DatabaseTestUtils {
    
    public void setupTestData(DataSource dataSource) {
        // Setup test data for integration tests
        try (Connection conn = dataSource.getConnection()) {
            // Insert test clients, accounts, holdings
            executeSqlScript(conn, "test-data.sql");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to setup test data", e);
        }
    }
    
    public void cleanupTestData(DataSource dataSource) {
        // Cleanup test data after tests
        try (Connection conn = dataSource.getConnection()) {
            executeSqlScript(conn, "cleanup-test-data.sql");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cleanup test data", e);
        }
    }
    
    private void executeSqlScript(Connection conn, String scriptName) throws SQLException {
        // Execute SQL script for test setup/cleanup
    }
}
```

## 11. Security Implementation

### 11.1 SQL Injection Prevention
```java
@Component
public class SqlSecurityValidator {
    
    public void validateSqlInput(String input) {
        if (input == null) {
            return;
        }
        
        // Check for SQL injection patterns
        String[] dangerousPatterns = {
            "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_",
            "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "EXEC"
        };
        
        String upperInput = input.toUpperCase();
        for (String pattern : dangerousPatterns) {
            if (upperInput.contains(pattern)) {
                throw new SecurityException("Potential SQL injection detected: " + pattern);
            }
        }
    }
    
    public String sanitizeSqlInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("['\";\\-\\-\\/\\*\\*]", "");
    }
}
```

### 11.2 Data Access Control
```java
@Component
public class DataAccessController {
    
    public boolean hasAccessToClient(String userId, String clientId) {
        // Implement client access control logic
        // Check if user is authorized to access client data
        return true; // Placeholder implementation
    }
    
    public boolean hasAccessToAccount(String userId, String accountId) {
        // Implement account access control logic
        // Check if user is authorized to access account data
        return true; // Placeholder implementation
    }
    
    public void validateDataAccess(String userId, String resourceType, String resourceId) {
        switch (resourceType.toLowerCase()) {
            case "client":
                if (!hasAccessToClient(userId, resourceId)) {
                    throw new AccessDeniedException("Access denied to client: " + resourceId);
                }
                break;
            case "account":
                if (!hasAccessToAccount(userId, resourceId)) {
                    throw new AccessDeniedException("Access denied to account: " + resourceId);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        }
    }
}
```

## 12. Integration Points

### 12.1 Spring Boot Auto-Configuration
```java
@Configuration
@ConditionalOnClass({JdbcTemplate.class, DataSource.class})
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DatabaseService databaseService(JdbcTemplate jdbcTemplate, 
                                         StoredProcedureExecutor storedProcedureExecutor) {
        return new DatabaseService(jdbcTemplate, storedProcedureExecutor);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public StoredProcedureExecutor storedProcedureExecutor(JdbcTemplate jdbcTemplate,
                                                         ParameterMapper parameterMapper,
                                                         ResultSetMapper resultSetMapper) {
        return new StoredProcedureExecutor(jdbcTemplate, parameterMapper, resultSetMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ParameterMapper parameterMapper() {
        return new ParameterMapper();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ResultSetMapper resultSetMapper() {
        return new ResultSetMapper();
    }
}
```

### 12.2 Maven Dependency Configuration
```xml
<dependency>
    <groupId>com.bny.shared</groupId>
    <artifactId>data-services-shared-resource</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Auto-configuration will be enabled via spring.factories -->
```

### 12.3 Properties Configuration
```java
@ConfigurationProperties(prefix = "bny.database")
@Data
public class DatabaseProperties {
    
    private boolean enableMetrics = true;
    private boolean enableCaching = true;
    private int maxQueryTimeout = 30;
    private boolean enableSqlValidation = true;
    private String[] allowedClients = {"domain-api", "lfd-api"};
    
    private VersionCompatibility version = new VersionCompatibility();
    private ConnectionPool connectionPool = new ConnectionPool();
    private Cache cache = new Cache();
    
    @Data
    public static class VersionCompatibility {
        private String minSpringBootVersion = "3.1.0";
        private String minJavaVersion = "17";
        private String recommendedSpringBootVersion = "3.2.0";
        private String recommendedJavaVersion = "21";
    }
    
    @Data
    public static class ConnectionPool {
        private int maxSize = 20;
        private int minIdle = 5;
        private long connectionTimeout = 20000;
        private long idleTimeout = 300000;
    }
    
    @Data
    public static class Cache {
        private int defaultTtlMinutes = 5;
        private String cacheType = "redis";
        private boolean enableCacheStatistics = true;
    }
}
```

## 13. Success Metrics and KPIs

### 13.1 Performance Metrics
- **Stored Procedure Execution Time**: 95th percentile < 200ms
- **Database Connection Acquisition Time**: 95th percentile < 50ms
- **Query Execution Time**: 95th percentile < 100ms
- **Connection Pool Utilization**: < 70% average

### 13.2 Reliability Metrics
- **Database Operation Success Rate**: > 99.8%
- **Connection Pool Timeout Rate**: < 0.01%
- **SQL Injection Prevention Rate**: 100% (no successful injections)
- **Data Validation Success Rate**: > 99.9%

### 13.3 Usage Metrics
- **Stored Procedure Calls**: Track usage patterns by procedure
- **Active Connections**: Monitor connection pool usage
- **Query Volume**: Track query execution volume
- **Error Rates**: Monitor error rates by operation type

## 14. Risk Assessment and Mitigation

### 14.1 Technical Risks
- **Connection Pool Exhaustion**: Mitigate with proper sizing and monitoring
- **SQL Injection Attacks**: Prevent with input validation and parameterized queries
- **Performance Degradation**: Monitor query performance and optimize indexes
- **Data Corruption**: Prevent with proper transaction management and validation

### 14.2 Security Risks
- **Unauthorized Data Access**: Implement proper access control mechanisms
- **Data Exposure**: Ensure proper data masking and encryption
- **Credential Exposure**: Use secure credential management
- **Audit Trail Gaps**: Implement comprehensive logging and monitoring

## 15. Future Enhancements

### 15.1 Phase 2 Enhancements
- **Multi-Database Support**: Support for PostgreSQL, Oracle in addition to MySQL
- **Advanced Caching**: Redis integration for distributed caching
- **Query Optimization**: Automatic query optimization and indexing suggestions
- **Data Encryption**: Field-level encryption for sensitive data

### 15.2 Phase 3 Enhancements
- **GraphQL Support**: GraphQL schema generation for database entities
- **Event Sourcing**: Database change event streaming
- **Machine Learning Integration**: Query performance prediction and optimization
- **Cloud Native**: Kubernetes-native database service integration

## 16. Acceptance Criteria

### 16.1 Functional Acceptance
1. All stored procedures execute successfully with proper parameter mapping
2. Result sets are correctly mapped to Java objects
3. Database connections are managed efficiently with proper pooling
4. Input validation prevents SQL injection attacks
5. Error handling provides meaningful error messages and codes
6. Transaction management ensures data consistency
7. Monitoring provides comprehensive visibility into database operations

### 16.2 Non-Functional Acceptance
1. Performance requirements are met under load testing
2. Security measures prevent unauthorized access and attacks
3. Reliability targets are achieved in production
4. Monitoring and alerting provide comprehensive visibility
5. Integration with Domain API and LFD API layers works seamlessly
6. Auto-configuration enables easy integration into consuming applications
7. Documentation is complete and accurate

## 17. Appendix

### A. Core Component Summary
| Component | Purpose | Key Features |
|-----------|---------|--------------|
| DatabaseService | Main database access service | Stored procedure execution, query execution |
| StoredProcedureExecutor | Stored procedure execution | Parameter mapping, result processing |
| ParameterMapper | SQL parameter mapping | Type conversion, validation |
| ResultSetMapper | Result set mapping | Entity mapping, DTO conversion |
| DatabaseExceptionMapper | Exception handling | Error code mapping, user-friendly messages |

### B. Configuration Parameters
| Parameter | Default | Description |
|-----------|---------|-------------|
| bny.database.enable-metrics | true | Enable database metrics collection |
| bny.database.enable-caching | true | Enable database query caching |
| bny.database.max-query-timeout | 30 | Maximum query timeout in seconds |
| bny.database.enable-sql-validation | true | Enable SQL injection validation |
| spring.datasource.primary.hikari.maximum-pool-size | 20 | Maximum primary connection pool size |
| spring.datasource.readonly.hikari.maximum-pool-size | 15 | Maximum read-only connection pool size |

### C. Error Codes
| Code | Description | Action |
|------|-------------|--------|
| DB_SYNTAX_ERROR | SQL syntax error in query or procedure | Check SQL syntax |
| DB_CONSTRAINT_VIOLATION | Database constraint violation | Check data validity |
| DB_TIMEOUT | Database operation timeout | Optimize query or increase timeout |
| DB_CONNECTION_ERROR | Database connection failure | Check connectivity |
| DB_RESOURCE_FAILURE | Database resource failure | Check resource availability |
| DB_UNKNOWN_ERROR | Unknown database error | Contact support |

### D. Monitoring Metrics
| Metric | Type | Description |
|--------|------|-------------|
| database.connections.active | Gauge | Active database connections |
| database.connections.idle | Gauge | Idle database connections |
| database.queries.executed | Counter | Total queries executed |
| database.queries.duration | Timer | Query execution time |
| database.stored-procedure.calls | Counter | Stored procedure calls |
| database.errors | Counter | Database error count |

### E. Integration Examples
```java
// Example usage in LFD API Layer
@Service
public class LfdClientService {
    
    @Autowired
    private DatabaseService databaseService;
    
    public ClientSearchResponse searchClients(ClientSearchRequest request) {
        Map<String, Object> parameters = Map.of(
            "p_advisor_id", request.getAdvisorId(),
            "p_search_query", request.getSearchQuery(),
            "p_page_offset", request.getPageOffset(),
            "p_page_size", request.getPageSize()
        );
        
        StoredProcedureResponse response = databaseService.executeProcedure(
            "sp_search_clients", parameters
        );
        
        return mapToClientSearchResponse(response);
    }
}
```
