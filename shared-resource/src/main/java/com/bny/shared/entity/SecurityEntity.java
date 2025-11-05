package com.bny.shared.entity;

import com.bny.shared.enums.AssetClass;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "securities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEntity {
    
    @Id
    @Column(name = "symbol", length = 20)
    private String symbol;
    
    @Column(name = "security_name", length = 200)
    private String securityName;
    
    @Column(name = "sector", length = 100)
    private String sector;
    
    @Column(name = "asset_class", length = 50)
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;
    
    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;
    
    @Column(name = "price_change", precision = 19, scale = 4)
    private BigDecimal priceChange;
    
    @Column(name = "price_change_percent", precision = 19, scale = 4)
    private BigDecimal priceChangePercent;
    
    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;
    
    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoldingEntity> holdings;
}
