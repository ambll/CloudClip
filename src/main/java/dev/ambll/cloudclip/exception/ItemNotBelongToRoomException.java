package dev.ambll.cloudclip.exception;

public class ItemNotBelongToRoomException extends RuntimeException {
    public  ItemNotBelongToRoomException() {
        super("Item does not belong to this room");
    }
}
