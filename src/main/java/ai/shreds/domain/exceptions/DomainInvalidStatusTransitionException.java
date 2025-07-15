package ai.shreds.domain.exceptions;

/**
 * Exception thrown when an invalid status transition is attempted.
 * This exception is used to enforce business rules around approval status transitions.
 */
public class DomainInvalidStatusTransitionException extends RuntimeException {
    
    /**
     * Creates a new exception with details about the invalid transition.
     * 
     * @param currentStatus the current status
     * @param targetStatus the target status that was attempted
     */
    public DomainInvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, targetStatus));
    }
    
    /**
     * Creates a new exception with a custom message.
     * 
     * @param message the exception message
     */
    public DomainInvalidStatusTransitionException(String message) {
        super(message);
    }
    
    /**
     * Creates a new exception with a custom message and cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     */
    public DomainInvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}