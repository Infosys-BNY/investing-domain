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
public class StoredProcedureResponse {
    private int resultCode;
    private String errorMessage;
    private Object data;
    private Map<String, Object> outputParameters;
    
    public static StoredProcedureResponse success(Object data) {
        return StoredProcedureResponse.builder()
            .resultCode(0)
            .data(data)
            .build();
    }
    
    public static StoredProcedureResponse error(int code, String message) {
        return StoredProcedureResponse.builder()
            .resultCode(code)
            .errorMessage(message)
            .build();
    }
}
