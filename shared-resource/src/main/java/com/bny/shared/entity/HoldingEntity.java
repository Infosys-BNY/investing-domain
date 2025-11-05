package com.bny.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "holdings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingEntity {
    
    @Id
    @Column(name = "holding_id", length = 50)
    private String holdingId;
    
    @Column(name = "account_id", length = 50, nullable = false)
    private String accountId;
    
    @Column(name = "symbol", length = 20, nullable = false)
    private String symbol;
    
    @Column(name = "quantity", precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "cost_basis", precision = 19, scale = 4)
    private BigDecimal costBasis;
    
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", insertable = false, updatable = false)
    private SecurityEntity security;
    
    @OneToMany(mappedBy = "holding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaxLotEntity> taxLots;
    
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
