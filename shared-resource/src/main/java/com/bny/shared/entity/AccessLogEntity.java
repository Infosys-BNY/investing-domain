package com.bny.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_log_id")
    private Long accessLogId;
    
    @Column(name = "account_id", length = 50, nullable = false)
    private String accountId;
    
    @Column(name = "advisor_id", length = 50)
    private String advisorId;
    
    @Column(name = "access_type", length = 100)
    private String accessType;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "request_id", length = 100)
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
