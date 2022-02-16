package com.dataart.blueprintsmanager.exceptions;

public class CustomApplicationException extends RuntimeException {
    public CustomApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
    public CustomApplicationException(String message) {
        super(message);
    }
}
