package com.dataart.blueprintsmanager.exceptions;

public class AuthenticationApplicationException extends CustomApplicationException {

    public AuthenticationApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationApplicationException(String message) {
        super(message);
    }
}
