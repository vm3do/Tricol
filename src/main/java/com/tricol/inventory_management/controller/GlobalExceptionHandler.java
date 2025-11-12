package com.tricol.inventory_management.controller;

import com.tricol.inventory_management.exception.DuplicateResourceException;
import com.tricol.inventory_management.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<HashMap<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("status", "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<HashMap<String, String>> handleDuplicate(DuplicateResourceException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("status", "409");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errors);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database constraint violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("foreign key")) {
                message = "Cannot perform this operation due to related data";
            } else if (ex.getMessage().contains("Duplicate entry")) {
                message = "This record already exists";
            }
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(message);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobal(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}

