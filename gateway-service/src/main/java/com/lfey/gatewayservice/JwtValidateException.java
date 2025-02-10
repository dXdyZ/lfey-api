package com.lfey.gatewayservice;

public class JwtValidateException extends RuntimeException {
    public JwtValidateException(String message, Throwable cause) {
        super(message, cause);
    }
}
