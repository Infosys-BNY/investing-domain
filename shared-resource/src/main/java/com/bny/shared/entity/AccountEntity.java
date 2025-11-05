package com.bny.shared.entity;

import com.bny.shared.enums.AccountType;
import com.bny.shared.enums.RiskProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {
    
    @Id
    @Column(name = "account_id", length = 50)
    private String accountId;
    
    @Column(name = "client_id", length = 50, nullable = false)
    private String clientId;
    
    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;
    
    @Column(name = "account_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @Column(name = "market_value", precision = 19, scale = 4)
    private BigDecimal marketValue;
    
    @Column(name = "cash_balance", precision = 19, scale = 4)
    private BigDecimal cashBalance;
    
    @Column(name = "ytd_performance", precision = 19, scale = 4)
    private BigDecimal ytdPerformance;
    
    @Column(name = "risk_profile", length = 50)
    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private ClientEntity client;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoldingEntity> holdings;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLogEntity> accessLogs;
    
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
