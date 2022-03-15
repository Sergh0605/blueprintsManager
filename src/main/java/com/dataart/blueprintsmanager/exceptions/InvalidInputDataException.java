package com.dataart.blueprintsmanager.exceptions;

public class InvalidInputDataException extends CustomApplicationException {

    public InvalidInputDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputDataException(String message) {
        super(message);
    }
}
