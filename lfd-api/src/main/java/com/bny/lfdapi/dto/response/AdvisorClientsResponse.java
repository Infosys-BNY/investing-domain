package com.bny.lfdapi.dto.response;

import com.bny.shared.dto.response.ClientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorClientsResponse {
    private List<ClientDto> clients;
    private Integer totalCount;
    private Integer pageOffset;
    private Integer pageSize;
    private Integer resultCode;
    private String errorMessage;
}
