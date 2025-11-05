package com.bny.lfdapi.service;

import com.bny.lfdapi.dto.response.StoredProcedureResponse;
import com.bny.lfdapi.exception.DatabaseOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StoredProcedureExecutor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, timeout = 30)
    public StoredProcedureResponse executeReadProcedure(String procedureName, Map<String, Object> parameters) {
        return executeProcedure(procedureName, parameters);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 30)
    public StoredProcedureResponse executeWriteProcedure(String procedureName, Map<String, Object> parameters) {
        return executeProcedure(procedureName, parameters);
    }

    private StoredProcedureResponse executeProcedure(String procedureName, Map<String, Object> parameters) {
        try {
            log.debug("Executing stored procedure: {} with parameters: {}", procedureName, parameters);
            
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(procedureName)
                .declareParameters(
                    new SqlOutParameter("p_result_code", Types.INTEGER),
                    new SqlOutParameter("p_error_message", Types.VARCHAR)
                );
            
            Map<String, Object> result = jdbcCall.execute(parameters != null ? parameters : new HashMap<>());
            
            Integer resultCode = (Integer) result.get("p_result_code");
            String errorMessage = (String) result.get("p_error_message");
            
            Object resultSetData = result.get("#result-set-1");
            
            StoredProcedureResponse response = StoredProcedureResponse.builder()
                .resultCode(resultCode)
                .errorMessage(errorMessage)
                .data(resultSetData)
                .outputParameters(result)
                .build();
            
            if (resultCode != null && resultCode != 0) {
                log.warn("Stored procedure {} returned non-zero result code: {} - {}", 
                    procedureName, resultCode, errorMessage);
            }
            
            log.debug("Stored procedure {} executed successfully with result code: {}", 
                procedureName, resultCode);
            
            return response;
            
        } catch (DataAccessException e) {
            log.error("Failed to execute stored procedure: {}", procedureName, e);
            throw new DatabaseOperationException(
                "Failed to execute stored procedure: " + procedureName, e);
        }
    }
}
