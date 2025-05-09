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
        public UserNotFoundException() {
            super("User not found");
        }
    }

    public static class UserInvalidAuthenticationException extends UserException {
        public UserInvalidAuthenticationException() {
            super("Invalid authentication");
        }
    }
}

