package dev.ambll.cloudclip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidRoomPasswordException.class)
    public ResponseEntity<String> handleInvalidRoomPassword(InvalidRoomPasswordException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handleRoomNotFound(RoomNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(RoomExpiredException.class)
    public ResponseEntity<String> handleRoomExpired(RoomExpiredException e) {
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(e.getMessage());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> handleItemNotFound(ItemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }
}
