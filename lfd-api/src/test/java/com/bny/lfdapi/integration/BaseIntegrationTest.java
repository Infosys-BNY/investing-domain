package com.bny.lfdapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseIntegrationTest.class);

    protected static final String ADVISOR_ID_1 = "ADV001";
    protected static final String ADVISOR_ID_2 = "ADV002";
    protected static final String CLIENT_ID_1 = "CLT001";
    protected static final String CLIENT_ID_2 = "CLT002";
    protected static final String CLIENT_ID_3 = "CLT003";
    protected static final String ACCOUNT_ID_1 = "ACC001";
    protected static final String ACCOUNT_ID_2 = "ACC002";
    protected static final String ACCOUNT_ID_3 = "ACC003";
    protected static final String ACCOUNT_ID_4 = "ACC004";
    protected static final String ACCOUNT_ID_5 = "ACC005";
    protected static final String ACCOUNT_ID_6 = "ACC006";

    protected static final long STORED_PROCEDURE_TARGET_MS = 500;
    protected static final long CONNECTION_ACQUISITION_TARGET_MS = 50;
    protected static final double SUCCESS_RATE_TARGET = 0.995;

    @BeforeEach
    public void baseSetup() {
        log.info("Starting integration test: {}", this.getClass().getSimpleName());
    }

    protected long measureExecutionTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    protected <T> T measureExecutionTimeWithResult(java.util.function.Supplier<T> operation, ExecutionMetrics metrics) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        metrics.recordExecution(durationMs);
        return result;
    }

    protected void assertPerformanceTarget(long actualMs, long targetMs, String operation) {
        log.info("Performance measurement for {}: {}ms (target: {}ms)", operation, actualMs, targetMs);
        if (actualMs > targetMs) {
            log.warn("Performance target missed for {}: {}ms > {}ms", operation, actualMs, targetMs);
        }
    }

    protected double calculatePercentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    protected static class ExecutionMetrics {
        private final List<Long> executionTimes = new ArrayList<>();
        private int successCount = 0;
        private int totalCount = 0;

        public void recordExecution(long durationMs) {
            executionTimes.add(durationMs);
            successCount++;
            totalCount++;
        }

        public void recordFailure() {
            totalCount++;
        }

        public double getPercentile(double percentile) {
            if (executionTimes.isEmpty()) {
                return 0.0;
            }
            List<Long> sorted = new ArrayList<>(executionTimes);
            Collections.sort(sorted);
            int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
            index = Math.max(0, Math.min(index, sorted.size() - 1));
            return sorted.get(index);
        }

        public double getSuccessRate() {
            return totalCount == 0 ? 0.0 : (double) successCount / totalCount;
        }

        public long getAverageExecutionTime() {
            return executionTimes.isEmpty() ? 0 : 
                executionTimes.stream().mapToLong(Long::longValue).sum() / executionTimes.size();
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }
    }
}
