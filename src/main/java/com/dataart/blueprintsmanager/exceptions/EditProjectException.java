package com.dataart.blueprintsmanager.exceptions;

public class EditProjectException extends CustomApplicationException {
    public EditProjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public EditProjectException(String message) {
        super(message);
    }
}
