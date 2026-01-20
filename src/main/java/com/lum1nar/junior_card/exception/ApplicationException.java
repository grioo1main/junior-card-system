package com.lum1nar.junior_card.exception;

public class ApplicationException extends RuntimeException{
    private final String errorCode;
    
    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ApplicationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
