package com.bny.investing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DomainApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainApiApplication.class, args);
    }
}
