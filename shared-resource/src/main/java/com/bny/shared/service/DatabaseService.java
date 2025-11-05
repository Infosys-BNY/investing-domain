package com.bny.shared.service;

import com.bny.shared.dto.common.DatabaseOperationResult;
import com.bny.shared.exception.DatabaseExceptionMapper;
import com.bny.shared.exception.DatabaseOperationException;
import com.bny.shared.util.validation.DatabaseInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DatabaseInputValidator inputValidator;
    
    @Autowired
    private DatabaseExceptionMapper exceptionMapper;
    
    public <T> List<T> query(String sql, Object[] params, RowMapper<T> rowMapper) {
        try {
            long startTime = System.currentTimeMillis();
            List<T> results = jdbcTemplate.query(sql, params, rowMapper);
            long executionTime = System.currentTimeMillis() - startTime;
            
            return results;
        } catch (Exception e) {
            throw exceptionMapper.mapException(e);
        }
    }
    
    public <T> T queryForObject(String sql, Object[] params, RowMapper<T> rowMapper) {
        try {
            return jdbcTemplate.queryForObject(sql, params, rowMapper);
        } catch (Exception e) {
            throw exceptionMapper.mapException(e);
        }
    }
    
    public DatabaseOperationResult update(String sql, Object[] params) {
        try {
            long startTime = System.currentTimeMillis();
            int affectedRows = jdbcTemplate.update(sql, params);
            long executionTime = System.currentTimeMillis() - startTime;
            
            return DatabaseOperationResult.builder()
                .success(true)
                .affectedRows(affectedRows)
                .executionTimeMs(executionTime)
                .build();
                
        } catch (Exception e) {
            DatabaseOperationException dbException = exceptionMapper.mapException(e);
            return DatabaseOperationResult.builder()
                .success(false)
                .affectedRows(0)
                .errorCode(dbException.getErrorCode())
                .errorMessage(dbException.getMessage())
                .executionTimeMs(0)
                .build();
        }
    }
    
    public List<Map<String, Object>> queryForList(String sql, Object[] params) {
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            throw exceptionMapper.mapException(e);
        }
    }
}
