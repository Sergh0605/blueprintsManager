package com.dataart.blueprintsmanager.exceptions;

public class EditDocumentException extends CustomApplicationException {
    public EditDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public EditDocumentException(String message) {
        super(message);
    }
}
