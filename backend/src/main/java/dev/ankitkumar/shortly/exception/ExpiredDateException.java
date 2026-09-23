package dev.ankitkumar.shortly.exception;

public class ExpiredDateException extends RuntimeException {
    public ExpiredDateException(String message) {
        super(message);
    }
}
