package com.dataart.blueprintsmanager.exceptions;

public class DataBaseCustomApplicationException extends CustomApplicationException{
    public DataBaseCustomApplicationException(String message) {
        super(message);
    }

    public DataBaseCustomApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
