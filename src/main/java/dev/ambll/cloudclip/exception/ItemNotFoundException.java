package dev.ambll.cloudclip.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("Item not found");
    }
}
