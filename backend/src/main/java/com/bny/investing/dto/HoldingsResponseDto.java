package com.bny.investing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingsResponseDto {
    private AccountDto accountInfo;
    private PortfolioSummaryDto summary;
    private List<HoldingDto> holdings;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
