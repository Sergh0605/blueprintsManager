package com.dataart.blueprintsmanager.exceptions;

public class TokenValidationException extends CustomApplicationException{

    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenValidationException(String message) {
        super(message);
    }
}
