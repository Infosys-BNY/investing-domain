package com.bny.shared.service;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ParameterMapper {
    
    public Map<String, Object> buildParameters(Map<String, Object> inputParameters) {
        if (inputParameters == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> parameters = new HashMap<>();
        for (Map.Entry<String, Object> entry : inputParameters.entrySet()) {
            parameters.put(entry.getKey(), convertValue(entry.getValue()));
        }
        
        return parameters;
    }
    
    public SqlParameter createSqlParameter(String paramName, Object value) {
        int sqlType = getSqlType(value);
        return new SqlParameter(paramName, sqlType);
    }
    
    public int getSqlType(Object value) {
        if (value == null) {
            return Types.NULL;
        }
        
        if (value instanceof String) {
            return Types.VARCHAR;
        } else if (value instanceof Integer) {
            return Types.INTEGER;
        } else if (value instanceof Long) {
            return Types.BIGINT;
        } else if (value instanceof Double || value instanceof Float) {
            return Types.DOUBLE;
        } else if (value instanceof BigDecimal) {
            return Types.DECIMAL;
        } else if (value instanceof Boolean) {
            return Types.BOOLEAN;
        } else if (value instanceof LocalDateTime) {
            return Types.TIMESTAMP;
        } else if (value instanceof java.sql.Date) {
            return Types.DATE;
        } else if (value instanceof java.sql.Timestamp) {
            return Types.TIMESTAMP;
        } else {
            return Types.VARCHAR;
        }
    }
    
    private Object convertValue(Object value) {
        if (value instanceof LocalDateTime) {
            return java.sql.Timestamp.valueOf((LocalDateTime) value);
        }
        return value;
    }
}
