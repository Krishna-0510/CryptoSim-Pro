package com.cryptosim.exception;

public class StaleDataException extends RuntimeException {

    public StaleDataException(String message) {
        super(message);
    }
}