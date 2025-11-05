package com.bny.shared.util.validation;

import com.bny.shared.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DatabaseInputValidator {
    
    private static final int MAX_STRING_LENGTH = 1000;
    private static final int MAX_DECIMAL_PLACES = 6;
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('.*(--|;|/\\*|\\*/|xp_|sp_|exec|execute|select|insert|update|delete|drop|create|alter|union|join).*')",
        Pattern.CASE_INSENSITIVE
    );
    
    public void validateProcedureParameters(String procedureName, Map<String, Object> parameters) {
        if (procedureName == null || procedureName.trim().isEmpty()) {
            throw new ValidationException("Procedure name cannot be null or empty", "procedureName");
        }
        
        if (containsSqlInjection(procedureName)) {
            throw new ValidationException("Procedure name contains invalid characters", "procedureName");
        }
        
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                validateParameterValue(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public void validateParameterValue(String paramName, Object value) {
        if (paramName == null || paramName.trim().isEmpty()) {
            throw new ValidationException("Parameter name cannot be null or empty");
        }
        
        if (value == null) {
            return;
        }
        
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.length() > MAX_STRING_LENGTH) {
                throw new ValidationException(
                    String.format("Parameter '%s' exceeds maximum length of %d characters", paramName, MAX_STRING_LENGTH),
                    paramName
                );
            }
            if (containsSqlInjection(strValue)) {
                throw new ValidationException(
                    String.format("Parameter '%s' contains potentially malicious content", paramName),
                    paramName
                );
            }
        }
    }
    
    public boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }
}
