package com.biblioteca.besone.exception;

public class LoanServiceException extends RuntimeException {
    public LoanServiceException(String message) {
        super(message);
    }

    public LoanServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public class LoanNotFoundException extends LoanServiceException {
        public LoanNotFoundException(String message) {
            super(message);
        }
    }

    public class BookNotAvailableException extends LoanServiceException {
        public BookNotAvailableException(String message) {
            super(message);
        }
    }

    public class InvalidLoanStatusException extends LoanServiceException {
        public InvalidLoanStatusException(String message) {
            super(message);
        }
    }

    public class DueDateExceededException extends LoanServiceException {
        public DueDateExceededException(String message) {
            super(message);
        }
    }
}