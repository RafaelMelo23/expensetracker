package com.github.rafaelmelo23.expense_tracker.exception;

public class ExpenseException extends RuntimeException {

    public ExpenseException(String message) {
        super(message);
    }

    public static class ExpenseNotFoundException extends ExpenseException {
        public ExpenseNotFoundException(Long expenseId) {
            super("Expense not found with ID: " + expenseId);
        }
    }

    public static class UserAccountingNotFoundException extends ExpenseException {
        public UserAccountingNotFoundException(Long userId) {
            super("User accounting not found for user ID: " + userId);
        }
    }

    public static class InvalidExpenseDataException extends ExpenseException {
        public InvalidExpenseDataException(String detail) {
            super("Invalid expense data: " + detail);
        }
    }

    public static class PersistenceException extends ExpenseException {
        public PersistenceException(String action, Throwable cause) {
            super("Failed to " + action + ": " + cause.getMessage());
            initCause(cause);
        }
    }
}
