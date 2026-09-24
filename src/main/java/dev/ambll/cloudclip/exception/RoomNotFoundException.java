package dev.ambll.cloudclip.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException() {
        super("Room not found");
    }
}
