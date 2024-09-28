package ru.ion.app.exception;

public class S3ServiceException extends RuntimeException {
    public S3ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
