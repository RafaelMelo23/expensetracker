package com.github.rafaelmelo23.expense_tracker.exception;

import com.github.rafaelmelo23.expense_tracker.exception.ExpenseException;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Void> handleUserExceptions(UserException ex, HttpServletRequest request) {
        if (ex instanceof UserException.UserNotAuthenticatedException) {
            log.error("[401] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.status(401).build();
        } else if (ex instanceof UserException.UserNotFoundException) {
            log.error("[404] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.status(404).build();
        } else if (ex instanceof UserException.UserInvalidAuthenticationException) {
            log.error("[403] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.status(403).build();
        }
        log.error("[400] {} - {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ExpenseException.class)
    public ResponseEntity<Void> handleExpenseExceptions(ExpenseException ex, HttpServletRequest request) {
        if (ex instanceof ExpenseException.ExpenseNotFoundException) {
            log.error("[404] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.status(404).build();
        } else if (ex instanceof ExpenseException.UserAccountingNotFoundException) {
            log.error("[400] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.badRequest().build();
        } else if (ex instanceof ExpenseException.InvalidExpenseDataException) {
            log.error("[422] {} - {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.unprocessableEntity().build();
        } else if (ex instanceof ExpenseException.PersistenceException) {
            log.error("[500] {} - {}", request.getRequestURI(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
        log.error("[400] {} - {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGenericExceptions(Exception ex, HttpServletRequest request) {
        log.error("[500] {} - Unexpected: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.internalServerError().build();
    }

}
