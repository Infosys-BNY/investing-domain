package com.bny.lfdapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedureResponse {
    private Integer resultCode;
    private String errorMessage;
    private Object data;
    private List<Map<String, Object>> resultSet;
    private Map<String, Object> outputParameters;
}
