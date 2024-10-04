package ru.ion.app.exception;

public class PasteServiceException extends RuntimeException {

    public PasteServiceException(String message) {
        super(message);
    }

    public PasteServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
