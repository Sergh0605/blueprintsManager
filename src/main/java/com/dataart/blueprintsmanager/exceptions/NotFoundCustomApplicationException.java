package com.dataart.blueprintsmanager.exceptions;

public class NotFoundCustomApplicationException extends CustomApplicationException {
    public NotFoundCustomApplicationException(String message) {
        super(message);
    }

    public NotFoundCustomApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
