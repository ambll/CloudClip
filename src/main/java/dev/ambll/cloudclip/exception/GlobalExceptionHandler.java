package dev.ambll.cloudclip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidRoomPassword(InvalidPasswordException e) {
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

    @ExceptionHandler(ItemNotBelongToRoomException.class)
    public ResponseEntity<String> handleItemNotBelongToRoom(ItemNotBelongToRoomException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(ItemNotFileException.class)
    public ResponseEntity<String> handleItemNotFile(ItemNotFileException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(RoomAccessDeniedException.class)
    public ResponseEntity<String> handleRoomAccessDenied(RoomAccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<String> handleStoredFileNotFound(StoredFileNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

    @ExceptionHandler(FileNotSavedException.class)
    public ResponseEntity<String> handleFileNotSaved(FileNotSavedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

    @ExceptionHandler(FileIsEmptyException.class)
    public ResponseEntity<String> handleFileIsEmpty(FileIsEmptyException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidFileNameException.class)
    public ResponseEntity<String> handleInvalidFileName(InvalidFileNameException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
