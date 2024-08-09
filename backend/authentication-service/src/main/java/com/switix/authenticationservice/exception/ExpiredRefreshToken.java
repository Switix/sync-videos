package com.switix.authenticationservice.exception;

public class ExpiredRefreshToken extends RuntimeException {
    public ExpiredRefreshToken(String message) {
        super(message);
    }
}
