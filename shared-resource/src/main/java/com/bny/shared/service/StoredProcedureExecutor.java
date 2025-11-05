package com.bny.shared.service;

import com.bny.shared.dto.common.StoredProcedureRequest;
import com.bny.shared.dto.common.StoredProcedureResponse;
import com.bny.shared.exception.DatabaseExceptionMapper;
import com.bny.shared.exception.DatabaseOperationException;
import com.bny.shared.util.validation.DatabaseInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StoredProcedureExecutor {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ParameterMapper parameterMapper;
    
    @Autowired
    private DatabaseInputValidator inputValidator;
    
    @Autowired
    private DatabaseExceptionMapper exceptionMapper;
    
    public StoredProcedureResponse execute(StoredProcedureRequest request) {
        try {
            inputValidator.validateProcedureParameters(request.getProcedureName(), request.getParameters());
            
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(request.getProcedureName());
            
            Map<String, Object> parameters = parameterMapper.buildParameters(request.getParameters());
            
            Map<String, Object> result = jdbcCall.execute(parameters);
            
            int resultCode = result.containsKey("p_result_code") ? 
                (Integer) result.get("p_result_code") : 0;
            String errorMessage = (String) result.get("p_error_message");
            Object data = result.get("#result-set-1");
            
            if (resultCode != 0) {
                return StoredProcedureResponse.error(resultCode, errorMessage);
            }
            
            return StoredProcedureResponse.builder()
                .resultCode(resultCode)
                .errorMessage(errorMessage)
                .data(data)
                .outputParameters(result)
                .build();
                
        } catch (Exception e) {
            DatabaseOperationException dbException = exceptionMapper.mapException(e);
            return StoredProcedureResponse.error(dbException.getResultCode(), dbException.getMessage());
        }
    }
}
