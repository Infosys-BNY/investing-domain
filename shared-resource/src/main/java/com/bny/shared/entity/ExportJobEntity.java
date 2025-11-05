package com.bny.shared.entity;

import com.bny.shared.enums.ExportFormat;
import com.bny.shared.enums.ExportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobEntity {
    
    @Id
    @Column(name = "job_id", length = 50)
    private String jobId;
    
    @Column(name = "account_id", length = 50, nullable = false)
    private String accountId;
    
    @Column(name = "export_format", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ExportFormat exportFormat;
    
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ExportStatus status;
    
    @Column(name = "filter_criteria", length = 500)
    private String filterCriteria;
    
    @Column(name = "requested_by", length = 100)
    private String requestedBy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "progress_percent")
    private Integer progressPercent;
    
    @Column(name = "download_url", length = 500)
    private String downloadUrl;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.progressPercent = 0;
    }
}
