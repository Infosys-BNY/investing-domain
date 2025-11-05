package com.bny.shared.exception;

import com.bny.shared.dto.common.DatabaseOperationResult;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class DatabaseExceptionMapper {
    
    public DatabaseOperationException mapException(Exception exception) {
        if (exception instanceof DataIntegrityViolationException) {
            return new DatabaseOperationException(
                "Data integrity violation: " + exception.getMessage(),
                exception,
                "INTEGRITY_VIOLATION",
                -2
            );
        } else if (exception instanceof QueryTimeoutException) {
            return new DatabaseOperationException(
                "Query timeout: " + exception.getMessage(),
                exception,
                "QUERY_TIMEOUT",
                -3
            );
        } else if (exception instanceof DataAccessException) {
            return new DatabaseOperationException(
                "Database access error: " + exception.getMessage(),
                exception,
                "DATA_ACCESS_ERROR",
                -4
            );
        } else if (exception instanceof SQLException) {
            SQLException sqlEx = (SQLException) exception;
            return new DatabaseOperationException(
                "SQL error: " + sqlEx.getMessage(),
                sqlEx,
                "SQL_ERROR_" + sqlEx.getErrorCode(),
                sqlEx.getErrorCode()
            );
        } else {
            return new DatabaseOperationException(
                "Unexpected database error: " + exception.getMessage(),
                exception,
                "UNKNOWN_ERROR",
                -1
            );
        }
    }
    
    public DatabaseOperationResult mapToErrorResponse(Exception exception) {
        DatabaseOperationException dbException = mapException(exception);
        
        return DatabaseOperationResult.builder()
            .success(false)
            .affectedRows(0)
            .errorCode(dbException.getErrorCode())
            .errorMessage(dbException.getMessage())
            .executionTimeMs(0)
            .build();
    }
}
