package ai.shreds.adapter.primary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ai.shreds.application.exceptions.ApplicationApprovalNotFoundException;
import ai.shreds.application.exceptions.ApplicationInvalidStatusTransitionException;
import ai.shreds.application.exceptions.ApplicationModeratorNotAuthorizedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the adapter layer to handle exceptions thrown by the application layer.
 */
@RestControllerAdvice
public class AdapterGlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdapterGlobalExceptionHandler.class);

    /**
     * Handles validation errors from request body validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");
        response.put("message", "Invalid request parameters");
        response.put("errors", errors);
        
        logger.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles approval request not found exceptions.
     */
    @ExceptionHandler(ApplicationApprovalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApprovalNotFound(
            ApplicationApprovalNotFoundException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Approval request not found");
        response.put("message", ex.getMessage());
        
        logger.warn("Approval request not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles invalid status transition exceptions.
     */
    @ExceptionHandler(ApplicationInvalidStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatusTransition(
            ApplicationInvalidStatusTransitionException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Invalid status transition");
        response.put("message", ex.getMessage());
        
        logger.warn("Invalid status transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles moderator authorization exceptions.
     */
    @ExceptionHandler(ApplicationModeratorNotAuthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleModeratorNotAuthorized(
            ApplicationModeratorNotAuthorizedException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Moderator not authorized");
        response.put("message", ex.getMessage());
        
        logger.warn("Moderator not authorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid argument");
        response.put("message", ex.getMessage());
        
        logger.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles all other runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred");
        
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred");
        
        logger.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(response);
    }
}