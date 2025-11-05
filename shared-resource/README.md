# BNY Data Services Shared Resource

Shared Spring Boot library providing database abstraction, DTOs, and utilities for Domain API and LFD API.

## Overview

This library provides:
- **JPA Entities**: Common data models (Client, Account, Holding, Security, TaxLot, AuditLog, AccessLog, ExportJob)
- **DTOs**: Request/Response data transfer objects with validation
- **Enumerations**: AccountType, AssetClass, RiskProfile, HoldingPeriod, ExportFormat, ExportStatus
- **Database Services**: DatabaseService, StoredProcedureExecutor with connection pooling
- **Utilities**: Validation, transformation, pagination helpers
- **Auto-Configuration**: Spring Boot auto-configuration for seamless integration

## Maven Coordinates

```xml
<dependency>
    <groupId>com.bny</groupId>
    <artifactId>data-services-shared-resource</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Installation

1. Build and install to local Maven repository:
```bash
cd shared-resource
mvn clean install
```

2. Add dependency to your project's `pom.xml`

## Configuration

The library uses Spring Boot auto-configuration. Configure database properties in your `application.yml`:

```yaml
bny:
  database:
    primary:
      maximum-pool-size: 20
      minimum-idle: 5
    read-only:
      maximum-pool-size: 15
      minimum-idle: 3
    query-timeout: 30
```

Environment variables:
- `DATABASE_URL`: Primary database JDBC URL
- `DATABASE_READONLY_URL`: Read-only database JDBC URL (optional)
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password

## Usage Examples

### Using DatabaseService

```java
@Service
public class ClientService {
    @Autowired
    private DatabaseService databaseService;
    
    public List<ClientDto> getClients(String advisorId) {
        String sql = "SELECT * FROM clients WHERE advisor_id = ?";
        return databaseService.query(sql, new Object[]{advisorId}, 
            (rs, rowNum) -> mapToClientDto(rs));
    }
}
```

### Using StoredProcedureExecutor

```java
@Service
public class PortfolioService {
    @Autowired
    private StoredProcedureExecutor procedureExecutor;
    
    public StoredProcedureResponse getPortfolio(String accountId) {
        StoredProcedureRequest request = StoredProcedureRequest.builder()
            .procedureName("sp_get_portfolio_summary")
            .parameters(Map.of("p_account_id", accountId))
            .build();
        
        return procedureExecutor.execute(request);
    }
}
```

### Using DTOs with Validation

```java
@RestController
public class ClientController {
    
    @PostMapping("/clients/search")
    public ResponseEntity<PaginatedResponse<ClientDto>> searchClients(
            @Valid @RequestBody ClientSearchRequest request) {
        // Request is automatically validated
        return ResponseEntity.ok(clientService.search(request));
    }
}
```

## Features

### Connection Pooling
- HikariCP with separate primary and read-only pools
- Configurable pool sizes and timeouts
- Automatic leak detection

### Validation
- SQL injection prevention
- Input parameter validation
- Jakarta Bean Validation annotations on DTOs

### Error Handling
- Standardized exception mapping
- Database operation result tracking
- Comprehensive error codes

### Performance
- Prepared statement caching
- Query timeout configuration
- Connection pool optimization

## Project Structure

```
shared-resource/
├── src/main/java/com/bny/shared/
│   ├── entity/           # JPA entities
│   ├── dto/              # Data transfer objects
│   │   ├── request/      # Request DTOs
│   │   ├── response/     # Response DTOs
│   │   └── common/       # Common DTOs
│   ├── enums/            # Enumerations
│   ├── service/          # Core services
│   ├── util/             # Utility classes
│   ├── exception/        # Exception handling
│   └── config/           # Configuration
└── src/main/resources/
    ├── META-INF/spring/  # Auto-configuration
    └── application.yml   # Default configuration
```

## Requirements

- Java 17+
- Spring Boot 3.2.x
- MySQL 8.0

## License

Copyright © 2024 BNY Mellon
