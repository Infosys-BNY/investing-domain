package com.bny.lfdapi.config;

import com.bny.lfdapi.security.InternalSecurityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityFilterConfig {

    @Bean
    public FilterRegistrationBean<InternalSecurityFilter> internalSecurityFilterRegistration(
            InternalSecurityFilter filter) {
        FilterRegistrationBean<InternalSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/internal/*");
        registration.setName("internalSecurityFilter");
        registration.setOrder(1);
        return registration;
    }
}
