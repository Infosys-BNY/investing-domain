package com.bny.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfig {
    
    @Autowired
    private DatabaseProperties databaseProperties;
    
    @Bean(name = "primaryDataSource")
    @Primary
    @ConditionalOnMissingBean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/investing"));
        config.setUsername(System.getenv().getOrDefault("DATABASE_USERNAME", "root"));
        config.setPassword(System.getenv().getOrDefault("DATABASE_PASSWORD", "password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        DatabaseProperties.Pool poolConfig = databaseProperties.getPrimary();
        config.setMaximumPoolSize(poolConfig.getMaximumPoolSize());
        config.setMinimumIdle(poolConfig.getMinimumIdle());
        config.setConnectionTimeout(poolConfig.getConnectionTimeout());
        config.setIdleTimeout(poolConfig.getIdleTimeout());
        config.setMaxLifetime(poolConfig.getMaxLifetime());
        config.setLeakDetectionThreshold(poolConfig.getLeakDetectionThreshold());
        
        config.setPoolName("BNY-Primary-Pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }
    
    @Bean(name = "readOnlyDataSource")
    @ConditionalOnMissingBean(name = "readOnlyDataSource")
    public DataSource readOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("DATABASE_READONLY_URL", 
            System.getenv().getOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/investing")));
        config.setUsername(System.getenv().getOrDefault("DATABASE_USERNAME", "root"));
        config.setPassword(System.getenv().getOrDefault("DATABASE_PASSWORD", "password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        DatabaseProperties.Pool poolConfig = databaseProperties.getReadOnly();
        config.setMaximumPoolSize(poolConfig.getMaximumPoolSize());
        config.setMinimumIdle(poolConfig.getMinimumIdle());
        config.setConnectionTimeout(poolConfig.getConnectionTimeout());
        config.setIdleTimeout(poolConfig.getIdleTimeout());
        config.setMaxLifetime(poolConfig.getMaxLifetime());
        
        config.setPoolName("BNY-ReadOnly-Pool");
        config.setReadOnly(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }
    
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(databaseProperties.getQueryTimeout());
        return template;
    }
    
    @Bean(name = "readOnlyJdbcTemplate")
    @ConditionalOnMissingBean(name = "readOnlyJdbcTemplate")
    public JdbcTemplate readOnlyJdbcTemplate(@Qualifier("readOnlyDataSource") DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(databaseProperties.getQueryTimeout());
        return template;
    }
    
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
