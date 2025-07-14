package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a moderator attempts to perform an action they are not authorized for.
 * This exception is used to enforce business rules around moderator authorization.
 */
public class DomainModeratorNotAuthorizedException extends RuntimeException {
    
    /**
     * Creates a new exception with details about the unauthorized moderator and request.
     * 
     * @param moderatorId the ID of the moderator who attempted the action
     * @param requestId the ID of the request they tried to access
     */
    public DomainModeratorNotAuthorizedException(String moderatorId, String requestId) {
        super(String.format("Moderator %s is not authorized to process request %s", moderatorId, requestId));
    }
    
    /**
     * Creates a new exception with details about the unauthorized moderator and action.
     * 
     * @param moderatorId the ID of the moderator who attempted the action
     * @param action the action they tried to perform
     */
    public DomainModeratorNotAuthorizedException(String moderatorId, String action) {
        super(String.format("Moderator %s is not authorized to perform action: %s", moderatorId, action));
    }
    
    /**
     * Creates a new exception with a custom message.
     * 
     * @param message the exception message
     */
    public DomainModeratorNotAuthorizedException(String message) {
        super(message);
    }
    
    /**
     * Creates a new exception with a custom message and cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     */
    public DomainModeratorNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}