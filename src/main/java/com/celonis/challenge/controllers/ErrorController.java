package com.celonis.challenge.controllers;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotAuthorizedException;
import com.celonis.challenge.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Map<String, String> handleNotFound(NotFoundException e) {
        logger.warn("Entity not found: {}", e.getMessage());
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Not found");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotAuthorizedException.class)
    public Map<String, String> handleNotAuthorized(NotAuthorizedException e) {
        logger.warn("Not authorized: {}", e.getMessage());
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Not authorized");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        logger.warn("Bad request: {}", e.getMessage());
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Bad request");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.warn("Validation failed: {}", e.getMessage());
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Validation failed");
        Map<String, String> fieldErrors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });

        if (!fieldErrors.isEmpty()) {
            errors.put("fieldErrors", fieldErrors);
        }

        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolation(ConstraintViolationException e) {
        logger.warn("Constraint violation: {}", e.getMessage());
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Validation error");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalException.class)
    public Map<String, String> handleInternalException(InternalException e) {
        logger.error("Internal error: {}", e.getMessage(), e);
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal error");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleInternalError(Exception e) {
        logger.error("Unhandled Exception in Controller", e);
        return Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal error");
    }
}
