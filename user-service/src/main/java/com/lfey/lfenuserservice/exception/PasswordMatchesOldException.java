package com.lfey.lfenuserservice.exception;

public class PasswordMatchesOldException extends RuntimeException {
    public PasswordMatchesOldException(String message) {
        super(message);
    }
}
