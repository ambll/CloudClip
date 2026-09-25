package dev.ambll.cloudclip.exception;

public class ItemNotFileException extends RuntimeException{
    public ItemNotFileException() {
        super("Item is not a file");
    }
}
