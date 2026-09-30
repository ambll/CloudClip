package dev.ambll.cloudclip.exception;

public class RoomAccessDeniedException extends RuntimeException {
    public RoomAccessDeniedException() {
        super("Access to the room is prohibited");
    }
}
