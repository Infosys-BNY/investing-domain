package com.bny.shared.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    private String clientId;
    
    @NotBlank(message = "Client name is required")
    @Size(max = 200, message = "Client name must not exceed 200 characters")
    private String clientName;
    
    @NotBlank(message = "Advisor ID is required")
    @Size(max = 50, message = "Advisor ID must not exceed 50 characters")
    private String advisorId;
    
    private LocalDateTime lastAccessed;
}
