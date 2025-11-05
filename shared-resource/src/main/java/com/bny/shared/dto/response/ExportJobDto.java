package com.bny.shared.dto.response;

import com.bny.shared.enums.ExportFormat;
import com.bny.shared.enums.ExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobDto {
    
    @NotBlank(message = "Job ID is required")
    private String jobId;
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;
    
    @NotNull(message = "Status is required")
    private ExportStatus status;
    
    private String filterCriteria;
    private String requestedBy;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
    private Integer progressPercent;
    private String downloadUrl;
    private String errorMessage;
}
