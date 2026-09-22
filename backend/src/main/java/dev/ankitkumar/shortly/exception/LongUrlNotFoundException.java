package dev.ankitkumar.shortly.exception;

public class LongUrlNotFoundException extends RuntimeException {
    public LongUrlNotFoundException(String message) {
        super(message);
    }
}
