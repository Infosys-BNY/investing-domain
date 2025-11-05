package com.bny.lfdapi.security;

import com.bny.lfdapi.exception.ValidationException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class InternalSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            validateInternalRequest(httpRequest);
            
            String userId = httpRequest.getHeader("X-User-ID");
            String advisorId = httpRequest.getHeader("X-Advisor-ID");
            String requestId = httpRequest.getHeader("X-Request-ID");
            String clientIp = httpRequest.getRemoteAddr();
            
            InternalRequestContext context = InternalRequestContext.builder()
                .userId(userId)
                .advisorId(advisorId)
                .requestId(requestId)
                .timestamp(LocalDateTime.now())
                .clientIp(clientIp)
                .build();
            
            InternalSecurityContextHolder.setContext(context);
            
            chain.doFilter(request, response);
        } catch (ValidationException e) {
            log.error("Internal request validation failed: {}", e.getMessage());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
        } finally {
            InternalSecurityContextHolder.clearContext();
        }
    }
    
    private void validateInternalRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        String advisorId = request.getHeader("X-Advisor-ID");
        String requestId = request.getHeader("X-Request-ID");
        
        if (!StringUtils.hasText(userId)) {
            throw new ValidationException("X-User-ID header is required");
        }
        
        if (!StringUtils.hasText(advisorId)) {
            throw new ValidationException("X-Advisor-ID header is required");
        }
        
        if (!StringUtils.hasText(requestId)) {
            throw new ValidationException("X-Request-ID header is required");
        }
    }
}
