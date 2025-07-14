package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a business rule is violated during the approval process.
 * This exception is used to indicate that critical business rules have been violated.
 */
public class DomainBusinessRuleViolationException extends RuntimeException {
    
    /**
     * Creates a new exception with rule name and details.
     * 
     * @param ruleName the name of the violated rule
     * @param details the details of the violation
     */
    public DomainBusinessRuleViolationException(String ruleName, String details) {
        super(String.format("Business rule violation: %s - %s", ruleName, details));
    }
    
    /**
     * Creates a new exception with a custom message.
     * 
     * @param message the exception message
     */
    public DomainBusinessRuleViolationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new exception with a custom message and cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     */
    public DomainBusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}