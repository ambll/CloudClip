package dev.ambll.cloudclip.exception;

public class StoredFileNotFoundException extends RuntimeException {
    public StoredFileNotFoundException() {
        super("Stored file is missing");
    }
}
