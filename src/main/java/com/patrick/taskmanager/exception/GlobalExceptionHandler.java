package com.patrick.taskmanager.exception;

import com.patrick.taskmanager.exception.conflict.ConflictException;
import com.patrick.taskmanager.exception.notfound.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException exc) {
        logger.warn("Access denied: {}", exc.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to perform this action");
    }

    @ExceptionHandler(InvalidTaskException.class)
    public ResponseEntity<Object> handleInvalidTask(InvalidTaskException exc) {
        logger.warn("Invalid task data: {}", exc.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", exc.getMessage());
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
        logger.warn("Constraint violation detected: {}", exc.getMessage());
        Map<String, String> fields = new TreeMap<>();

        for (ConstraintViolation<?> violation : exc.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();

            if (fieldName.contains(".")) {
                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            }

            fields.put(fieldName, violation.getMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Validation error",
                "Validation failed for one or more constraints.",
                fields);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException exc) {
        logger.warn("Invalid credentials: {}", exc.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", exc.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exc) {
        logger.warn("Method argument not valid: {}", exc.getMessage());
        Map<String, String> fields = new TreeMap<>();

        for (FieldError fieldError : exc.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Validation error",
                "Validation failed for one or more fields.",
                fields);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionException(TransactionSystemException exc) {
        Throwable cause = exc;
        while (cause != null) {
            if (cause instanceof InvalidTaskException ite) {
                return handleInvalidTask(ite);
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

    @SuppressWarnings("SameParameterValue")
    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message, Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("fields", fields);
        return new ResponseEntity<>(body, status);
    }
}
