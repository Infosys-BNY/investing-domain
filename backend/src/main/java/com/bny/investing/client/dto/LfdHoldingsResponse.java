package com.bny.investing.client.dto;

import com.bny.shared.dto.response.HoldingDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LfdHoldingsResponse {
    private List<HoldingDto> holdings;
    private Integer totalCount;
    private Integer pageOffset;
    private Integer pageSize;
    private Integer resultCode;
    private String errorMessage;
}
