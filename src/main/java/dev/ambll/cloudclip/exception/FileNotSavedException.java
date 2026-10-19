package dev.ambll.cloudclip.exception;

public class FileNotSavedException extends RuntimeException {
    public FileNotSavedException(Throwable cause) {
        super("File was not saved", cause);
    }
}
