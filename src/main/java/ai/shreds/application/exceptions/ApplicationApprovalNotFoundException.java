package ai.shreds.application.exceptions;

/**
 * Exception thrown when an approval request is not found in the system.
 * This exception is used to indicate that a requested approval process
 * could not be located or accessed.
 */
public class ApplicationApprovalNotFoundException extends RuntimeException {

    /**
     * Constructs a new ApplicationApprovalNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ApplicationApprovalNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ApplicationApprovalNotFoundException with the specified request ID and cause.
     *
     * @param requestId the ID of the approval request that was not found
     * @param cause the cause of the exception
     */
    public ApplicationApprovalNotFoundException(String requestId, Throwable cause) {
        super("Approval request not found: " + requestId, cause);
    }

    /**
     * Constructs a new ApplicationApprovalNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception
     */
    public ApplicationApprovalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}