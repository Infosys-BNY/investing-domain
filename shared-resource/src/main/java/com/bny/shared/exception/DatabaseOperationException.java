package com.bny.shared.exception;

public class DatabaseOperationException extends RuntimeException {
    private final String errorCode;
    private final int resultCode;
    
    public DatabaseOperationException(String message) {
        super(message);
        this.errorCode = "DB_ERROR";
        this.resultCode = -1;
    }
    
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DB_ERROR";
        this.resultCode = -1;
    }
    
    public DatabaseOperationException(String message, String errorCode, int resultCode) {
        super(message);
        this.errorCode = errorCode;
        this.resultCode = resultCode;
    }
    
    public DatabaseOperationException(String message, Throwable cause, String errorCode, int resultCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.resultCode = resultCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getResultCode() {
        return resultCode;
    }
}
