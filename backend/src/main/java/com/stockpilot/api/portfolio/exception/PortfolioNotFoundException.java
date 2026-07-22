package com.stockpilot.api.portfolio.exception;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(String message) {
        super(message);
    }
}
