package dev.ambll.cloudclip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidRoomPasswordException.class)
    public ResponseEntity<Void> handleInvalidRoomPassword() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }
}
