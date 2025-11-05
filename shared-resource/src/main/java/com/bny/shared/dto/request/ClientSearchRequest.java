package com.bny.shared.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSearchRequest {
    
    @NotBlank(message = "Advisor ID is required")
    @Size(max = 50, message = "Advisor ID must not exceed 50 characters")
    private String advisorId;
    
    @Size(max = 200, message = "Search query must not exceed 200 characters")
    private String searchQuery;
    
    @Min(value = 0, message = "Page offset must be 0 or greater")
    private int pageOffset = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    private int pageSize = 50;
}
