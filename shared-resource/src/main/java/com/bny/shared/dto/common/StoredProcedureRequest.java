package com.bny.shared.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedureRequest {
    private String procedureName;
    private Map<String, Object> parameters;
    private Map<String, Object> outputParameters;
}
