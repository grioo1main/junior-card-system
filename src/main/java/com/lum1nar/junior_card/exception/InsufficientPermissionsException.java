package com.lum1nar.junior_card.exception;

public class InsufficientPermissionsException extends ApplicationException{

    public InsufficientPermissionsException(String message, String errorCode) {
        super(message, errorCode);
    }

    public InsufficientPermissionsException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
}
