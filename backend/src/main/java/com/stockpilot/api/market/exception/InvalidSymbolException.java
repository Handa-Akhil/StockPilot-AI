package com.stockpilot.api.market.exception;

public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String message) {
        super(message);
    }
}
