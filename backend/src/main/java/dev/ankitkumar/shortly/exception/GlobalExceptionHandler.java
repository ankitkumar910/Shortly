package dev.ankitkumar.shortly.exception;

import dev.ankitkumar.shortly.dto.ExceptionResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LongUrlNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> longUrlNotFoundExceptionHandler(LongUrlNotFoundException exception) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto();
        exceptionResponseDto.setMessage(exception.getLocalizedMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponseDto);
    }

    @ExceptionHandler({
            InvalidShortCodeException.class,
            DuplicateEntryException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ExceptionResponseDto> invalidShortCodeExceptionHandler(RuntimeException exception) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto();
        exceptionResponseDto.setMessage(exception.getLocalizedMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exceptionResponseDto);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponseDto> constraintViolationExceptionHandler(ConstraintViolationException exception) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto();

        List<String> messages = new ArrayList<>();

        exception.getConstraintViolations().forEach(e -> messages.add(e.getMessage()));

        exceptionResponseDto.setMessage(messages.toString());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exceptionResponseDto);
    }
}
