package com.bny.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {
    
    @Id
    @Column(name = "client_id", length = 50)
    private String clientId;
    
    @Column(name = "client_name", length = 200, nullable = false)
    private String clientName;
    
    @Column(name = "advisor_id", length = 50, nullable = false)
    private String advisorId;
    
    @Column(name = "tax_id", length = 20)
    private String taxId;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountEntity> accounts;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLogEntity> auditLogs;
    
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
