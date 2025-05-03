package com.github.rafaelmelo23.expense_tracker.exception;

public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public static class UserNotAuthenticatedException extends UserException {
        public UserNotAuthenticatedException() {
            super("User is not authenticated.");
        }
    }

    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(Long userId) {
            super("User not found with ID: " + userId);
        }
    }
}

