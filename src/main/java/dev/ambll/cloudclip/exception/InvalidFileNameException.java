package dev.ambll.cloudclip.exception;

public class InvalidFileNameException extends RuntimeException {
    public InvalidFileNameException() {
        super("Invalid file name");
    }
}
