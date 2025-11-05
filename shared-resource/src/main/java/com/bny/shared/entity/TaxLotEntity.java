package com.bny.shared.entity;

import com.bny.shared.enums.HoldingPeriod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_lots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxLotEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_lot_id")
    private Long taxLotId;
    
    @Column(name = "holding_id", length = 50, nullable = false)
    private String holdingId;
    
    @Column(name = "lot_number")
    private Integer lotNumber;
    
    @Column(name = "quantity", precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "cost_basis", precision = 19, scale = 4)
    private BigDecimal costBasis;
    
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column(name = "holding_period", length = 20)
    @Enumerated(EnumType.STRING)
    private HoldingPeriod holdingPeriod;
    
    @Column(name = "tax_impact_estimate", precision = 19, scale = 4)
    private BigDecimal taxImpactEstimate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", insertable = false, updatable = false)
    private HoldingEntity holding;
}
