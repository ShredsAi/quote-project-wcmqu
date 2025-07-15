package ai.shreds.application.exceptions;

/**
 * Exception thrown when an invalid status transition is attempted in the approval workflow.
 * This exception indicates that the requested status change violates the business rules
 * or workflow constraints.
 */
public class ApplicationInvalidStatusTransitionException extends RuntimeException {

    /**
     * Constructs a new ApplicationInvalidStatusTransitionException with details about the invalid transition.
     *
     * @param currentStatus the current status of the approval request
     * @param targetStatus the target status that was attempted
     */
    public ApplicationInvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition from '%s' to '%s'", currentStatus, targetStatus));
    }

    /**
     * Constructs a new ApplicationInvalidStatusTransitionException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception
     */
    public ApplicationInvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ApplicationInvalidStatusTransitionException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ApplicationInvalidStatusTransitionException(String message) {
        super(message);
    }
}