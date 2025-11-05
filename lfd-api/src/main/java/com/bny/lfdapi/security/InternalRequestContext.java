package com.bny.lfdapi.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalRequestContext {
    private String userId;
    private String advisorId;
    private String requestId;
    private LocalDateTime timestamp;
    private String clientIp;
}
