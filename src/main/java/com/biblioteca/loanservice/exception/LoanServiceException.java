package com.biblioteca.loanservice.exception;

public class LoanServiceException extends RuntimeException {
    public LoanServiceException(String message) {
        super(message);
    }

    public LoanServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}