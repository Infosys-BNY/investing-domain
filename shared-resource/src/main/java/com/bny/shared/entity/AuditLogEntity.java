package com.bny.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long auditLogId;
    
    @Column(name = "client_id", length = 50)
    private String clientId;
    
    @Column(name = "user_id", length = 50)
    private String userId;
    
    @Column(name = "action_type", length = 100)
    private String actionType;
    
    @Column(name = "action_details", columnDefinition = "TEXT")
    private String actionDetails;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "request_id", length = 100)
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private ClientEntity client;
    
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
