package com.example.hamzabackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * Constructs a new BadRequestException with the specified detail message.
     * @param message the detail message.
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new BadRequestException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
