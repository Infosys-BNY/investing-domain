package com.bny.shared.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ComponentScan(basePackages = {
    "com.bny.shared.service",
    "com.bny.shared.util",
    "com.bny.shared.exception"
})
@Import({DatabaseConfig.class, TransactionConfig.class})
public class DatabaseAutoConfiguration {
}
