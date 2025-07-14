package ai.shreds.application.exceptions;

/**
 * Exception thrown when a moderator is not authorized to perform a specific action
 * in the approval workflow. This exception indicates that the moderator lacks
 * the necessary permissions or is not assigned to the approval request.
 */
public class ApplicationModeratorNotAuthorizedException extends RuntimeException {

    /**
     * Constructs a new ApplicationModeratorNotAuthorizedException with moderator and action details.
     *
     * @param moderatorId the ID of the moderator who is not authorized
     * @param action the action that the moderator attempted to perform
     */
    public ApplicationModeratorNotAuthorizedException(String moderatorId, String action) {
        super(String.format("Moderator '%s' is not authorized to perform action: %s", moderatorId, action));
    }

    /**
     * Constructs a new ApplicationModeratorNotAuthorizedException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ApplicationModeratorNotAuthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new ApplicationModeratorNotAuthorizedException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception
     */
    public ApplicationModeratorNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}