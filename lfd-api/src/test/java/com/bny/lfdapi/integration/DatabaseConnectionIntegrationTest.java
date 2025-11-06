package com.bny.lfdapi.integration;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Database Connection Pool Integration Tests")
public class DatabaseConnectionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should successfully connect to AWS RDS database")
    public void database_ValidatesConnectivity() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        
        assertThat(result).isEqualTo(1);
        log.info("Database connectivity validated successfully");
    }

    @Test
    @DisplayName("Should verify HikariCP connection pool configuration")
    public void connectionPool_VerifiesPoolConfiguration() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        
        assertThat(hikariDataSource.getMaximumPoolSize()).isEqualTo(10);
        assertThat(hikariDataSource.getMinimumIdle()).isEqualTo(2);
        assertThat(hikariDataSource.getConnectionTimeout()).isEqualTo(20000);
        
        log.info("HikariCP Configuration:");
        log.info("  Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
        log.info("  Minimum Idle: {}", hikariDataSource.getMinimumIdle());
        log.info("  Connection Timeout: {} ms", hikariDataSource.getConnectionTimeout());
        log.info("  Idle Timeout: {} ms", hikariDataSource.getIdleTimeout());
        log.info("  Max Lifetime: {} ms", hikariDataSource.getMaxLifetime());
    }

    @Test
    @DisplayName("Should acquire connections within timeout")
    public void connectionPool_AcquiresConnection_WithinTimeout() throws SQLException {
        ExecutionMetrics metrics = new ExecutionMetrics();
        
        for (int i = 0; i < 10; i++) {
            long startTime = System.nanoTime();
            try (Connection connection = dataSource.getConnection()) {
                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                metrics.recordExecution(durationMs);
                
                assertThat(connection).isNotNull();
                assertThat(connection.isClosed()).isFalse();
            }
        }
        
        double p95 = metrics.getPercentile(95);
        long avgTime = metrics.getAverageExecutionTime();
        
        log.info("Connection Acquisition Performance:");
        log.info("  Average Time: {} ms", avgTime);
        log.info("  95th Percentile: {} ms", p95);
        
        assertPerformanceTarget(avgTime, CONNECTION_ACQUISITION_TARGET_MS, "connection acquisition");
        
        if (p95 > CONNECTION_ACQUISITION_TARGET_MS) {
            log.warn("95th percentile ({} ms) exceeds target ({} ms)", p95, CONNECTION_ACQUISITION_TARGET_MS);
        }
    }

    @Test
    @DisplayName("Should handle concurrent connection requests")
    public void connectionPool_HandlesConcurrentRequests() throws InterruptedException, ExecutionException {
        int numberOfThreads = 5;
        int requestsPerThread = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < numberOfThreads; i++) {
            Future<Boolean> future = executorService.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try (Connection connection = dataSource.getConnection()) {
                        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                        assertThat(result).isEqualTo(1);
                    } catch (SQLException e) {
                        log.error("Failed to get connection", e);
                        return false;
                    }
                }
                return true;
            });
            futures.add(future);
        }
        
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();
        
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }
        
        double successRate = (double) successCount / numberOfThreads;
        log.info("Concurrent Requests Test:");
        log.info("  Threads: {}", numberOfThreads);
        log.info("  Requests per Thread: {}", requestsPerThread);
        log.info("  Success Rate: {} %", successRate * 100);
        
        assertThat(successRate).isGreaterThanOrEqualTo(SUCCESS_RATE_TARGET);
    }

    @Test
    @DisplayName("Should track connection pool metrics")
    public void connectionPool_TracksMetrics() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        
        if (poolMXBean != null) {
            log.info("HikariCP Pool Metrics:");
            log.info("  Active Connections: {}", poolMXBean.getActiveConnections());
            log.info("  Idle Connections: {}", poolMXBean.getIdleConnections());
            log.info("  Total Connections: {}", poolMXBean.getTotalConnections());
            log.info("  Threads Awaiting Connection: {}", poolMXBean.getThreadsAwaitingConnection());
            
            assertThat(poolMXBean.getTotalConnections()).isLessThanOrEqualTo(hikariDataSource.getMaximumPoolSize());
        } else {
            log.warn("HikariCP MXBean not available - metrics tracking skipped");
        }
    }

    @Test
    @DisplayName("Should verify connection is valid")
    public void connectionPool_VerifiesConnectionValidity() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isClosed()).isFalse();
            assertThat(connection.isValid(5)).isTrue();
            
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            String dbProductVersion = connection.getMetaData().getDatabaseProductVersion();
            
            log.info("Database Connection Details:");
            log.info("  Product Name: {}", dbProductName);
            log.info("  Product Version: {}", dbProductVersion);
            
            assertThat(dbProductName).containsIgnoringCase("MySQL");
        }
    }

    @Test
    @DisplayName("Should execute simple query successfully")
    public void database_ExecutesQuerySuccessfully() {
        String query = "SELECT COUNT(*) FROM clients";
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        Integer count = measureExecutionTimeWithResult(
            () -> jdbcTemplate.queryForObject(query, Integer.class),
            metrics
        );
        
        assertThat(count).isNotNull().isGreaterThanOrEqualTo(0);
        log.info("Query '{}' returned count: {}", query, count);
        log.info("Execution time: {} ms", metrics.getAverageExecutionTime());
    }

    @Test
    @DisplayName("Should maintain connection pool under load")
    public void connectionPool_MaintainsPoolUnderLoad() throws InterruptedException {
        int iterations = 50;
        ExecutionMetrics metrics = new ExecutionMetrics();
        
        for (int i = 0; i < iterations; i++) {
            try {
                measureExecutionTimeWithResult(() -> {
                    return jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                }, metrics);
            } catch (Exception e) {
                metrics.recordFailure();
                log.error("Query execution {} failed", i, e);
            }
            
            Thread.sleep(10);
        }
        
        double successRate = metrics.getSuccessRate();
        long avgTime = metrics.getAverageExecutionTime();
        
        log.info("Load Test Results:");
        log.info("  Total Executions: {}", iterations);
        log.info("  Successful: {} ({} %)", metrics.getSuccessCount(), successRate * 100);
        log.info("  Average Time: {} ms", avgTime);
        
        assertThat(successRate).isGreaterThanOrEqualTo(SUCCESS_RATE_TARGET);
    }

    @Test
    @DisplayName("Should verify JDBC URL is correct")
    public void connectionPool_VerifiesJdbcUrl() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        String jdbcUrl = hikariDataSource.getJdbcUrl();
        
        assertThat(jdbcUrl).contains("bny-demo.c3uyq60ukgb6.us-east-2.rds.amazonaws.com");
        assertThat(jdbcUrl).contains("bny_data_services");
        
        log.info("JDBC URL verified: {}", jdbcUrl);
    }
}
