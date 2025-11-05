package com.bny.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bny.database")
public class DatabaseProperties {
    
    private Pool primary = new Pool();
    private Pool readOnly = new Pool();
    private int queryTimeout = 30;
    private boolean enableMetrics = true;
    private boolean enableCaching = true;
    private int cacheTimeoutSeconds = 300;
    
    @Data
    public static class Pool {
        private int maximumPoolSize = 20;
        private int minimumIdle = 5;
        private long connectionTimeout = 20000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private long leakDetectionThreshold = 60000;
    }
}
