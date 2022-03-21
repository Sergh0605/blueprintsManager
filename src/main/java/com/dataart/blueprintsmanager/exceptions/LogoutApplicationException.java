package com.dataart.blueprintsmanager.exceptions;

public class LogoutApplicationException extends CustomApplicationException {

    public LogoutApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogoutApplicationException(String message) {
        super(message);
    }
}
