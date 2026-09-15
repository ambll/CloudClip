package dev.ambll.cloudclip.exception;

public class InvalidRoomPasswordException extends RuntimeException {

    public InvalidRoomPasswordException() {
        super("Invalid room password");
    }
}