package com.bny.shared.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseOperationResult {
    private boolean success;
    private int affectedRows;
    private Object resultData;
    private String errorCode;
    private String errorMessage;
    private long executionTimeMs;
}
