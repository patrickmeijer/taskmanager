package com.patrick.taskmanager.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", exc.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException exc) {
        logger.warn("Resource not found: {}", exc.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not found", exc.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflict(ConflictException exc) {
        logger.warn("Conflict detected: {}", exc.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", exc.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException exc) {
        String message = exc.getConstraintViolations().iterator().next().getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", message);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException exc) {
        logger.warn("Invalid credentials: {}", exc.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", exc.getMessage());
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionException(TransactionSystemException exc) {
        Throwable cause = exc;
        while (cause != null) {
            if (cause instanceof IllegalStateException ise) {
                return handleIllegalState(ise);
            }
            if (cause instanceof ConstraintViolationException cve) {
                return handleConstraintViolation(cve);
            }
            cause = cause.getCause();
        }
        logger.error("Transaction failed due to underlying system issue", exc);
        return handleGeneralException(exc);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exc) {
        logger.error("Unexpected error: {}", exc.getMessage(), exc);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred while processing the request. Please try again.");
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
