package com.bny.shared.dto.request;

import com.bny.shared.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    
    @NotBlank(message = "Account ID is required")
    @Size(max = 50, message = "Account ID must not exceed 50 characters")
    private String accountId;
    
    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;
    
    @Size(max = 500, message = "Filter criteria must not exceed 500 characters")
    private String filterCriteria;
    
    @NotBlank(message = "Requested by is required")
    @Size(max = 100, message = "Requested by must not exceed 100 characters")
    private String requestedBy;
}
