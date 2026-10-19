package dev.ambll.cloudclip.exception;

public class FileIsEmptyException extends RuntimeException{
    public FileIsEmptyException() {
        super("File is empty");
    }
}
