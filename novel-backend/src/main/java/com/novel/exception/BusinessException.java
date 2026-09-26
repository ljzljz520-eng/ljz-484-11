package com.novel.exception;

/**
 * Thrown when a request violates business rules (e.g. blank title,
 * invalid chapter status). Mapped to HTTP 400 by GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
