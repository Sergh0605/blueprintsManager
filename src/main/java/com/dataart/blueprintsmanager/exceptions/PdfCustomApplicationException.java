package com.dataart.blueprintsmanager.exceptions;

public class PdfCustomApplicationException extends CustomApplicationException{
    public PdfCustomApplicationException(String message) {
        super(message);
    }

    public PdfCustomApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
