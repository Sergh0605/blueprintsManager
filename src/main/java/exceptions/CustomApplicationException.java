package exceptions;

public class CustomApplicationException extends RuntimeException {
    public CustomApplicationException(String message) {
        super(message);
    }
}
