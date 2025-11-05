package com.bny.lfdapi.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class LfdHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("connectionPoolActive", true)
                        .build();
                }
            }
            return Health.down().withDetail("database", "Connection failed").build();
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("database", "Error: " + e.getMessage())
                .build();
        }
    }
}
