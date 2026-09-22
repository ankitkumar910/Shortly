package dev.ankitkumar.shortly.exception;

public class InvalidShortCodeException extends IllegalArgumentException {
    public InvalidShortCodeException(String s) {
        super(s);
    }
}
