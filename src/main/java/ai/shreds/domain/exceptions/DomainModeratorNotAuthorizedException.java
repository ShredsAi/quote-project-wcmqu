package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a moderator attempts to perform an action they are not authorized for.
 * This exception is used to enforce business rules around moderator authorization.
 */
public class DomainModeratorNotAuthorizedException extends RuntimeException {
    
    private final String moderatorId;
    private final String requestId;
    
    /**
     * Creates a new exception with details about the unauthorized moderator and request.
     * 
     * @param moderatorId the ID of the moderator who attempted the action
     * @param requestId the ID of the request they tried to access
     */
    public DomainModeratorNotAuthorizedException(String moderatorId, String requestId) {
        super(String.format("Moderator %s is not authorized to process request %s", moderatorId, requestId));
        this.moderatorId = moderatorId;
        this.requestId = requestId;
    }
    
    /**
     * Creates a new exception with a custom message.
     * 
     * @param message the exception message
     */
    public DomainModeratorNotAuthorizedException(String message) {
        super(message);
        this.moderatorId = null;
        this.requestId = null;
    }
    
    /**
     * Creates a new exception with a custom message and cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     */
    public DomainModeratorNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.moderatorId = null;
        this.requestId = null;
    }
    
    /**
     * Gets the moderator ID.
     * 
     * @return the moderator ID
     */
    public String getModeratorId() {
        return moderatorId;
    }
    
    /**
     * Gets the request ID.
     * 
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return "MODERATOR_NOT_AUTHORIZED";
    }
}