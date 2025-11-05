package com.bny.shared.dto.response;

import com.bny.shared.enums.HoldingPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxLotDto {
    
    private Long taxLotId;
    
    @NotNull(message = "Holding ID is required")
    private String holdingId;
    
    private Integer lotNumber;
    
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    
    private BigDecimal costBasis;
    private LocalDateTime purchaseDate;
    private HoldingPeriod holdingPeriod;
    private BigDecimal taxImpactEstimate;
}
