package com.bny.investing.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "app.mock.enabled=false",
    "lfd.api.base-url=http://localhost:8081",
    "server.port=8080",
    "spring.datasource.url=jdbc:mysql://bny-demo.c3uyq60ukgb6.us-east-2.rds.amazonaws.com:3306/bny_data_services",
    "spring.datasource.username=admin",
    "spring.datasource.password=password",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect"
})
public abstract class IntegrationTestBase {
    
    @BeforeAll
    static void setupIntegrationTests() {
        System.out.println("========================================");
        System.out.println("Running integration tests against AWS RDS database");
        System.out.println("Database: bny-demo.c3uyq60ukgb6.us-east-2.rds.amazonaws.com:3306/bny_data_services");
        System.out.println("Ensure LFD API is running on port 8081");
        System.out.println("========================================");
    }
}
