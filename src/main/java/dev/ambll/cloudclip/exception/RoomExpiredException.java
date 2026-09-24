package dev.ambll.cloudclip.exception;

public class RoomExpiredException extends RuntimeException {
    public RoomExpiredException() {
        super("Room is expired");
    }
}
