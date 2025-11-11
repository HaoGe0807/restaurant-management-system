package com.company.ecommerce.common.exception;

/**
 * 领域异常基类
 */
public class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

