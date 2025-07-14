package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when there are issues with messaging operations in the infrastructure layer.
 * This exception typically wraps underlying messaging system errors such as RabbitMQ connection issues,
 * queue errors, or message publishing failures.
 */
public class InfrastructureMessagingException extends RuntimeException {

    /**
     * Constructs a new InfrastructureMessagingException with the specified detail message.
     * 
     * @param message the detail message explaining the reason for the exception
     */
    public InfrastructureMessagingException(String message) {
        super(message);
    }

    /**
     * Constructs a new InfrastructureMessagingException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of this exception
     */
    public InfrastructureMessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InfrastructureMessagingException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public InfrastructureMessagingException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new InfrastructureMessagingException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * 
     * @param message the detail message
     * @param cause the underlying cause
     * @param enableSuppression whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public InfrastructureMessagingException(String message, Throwable cause, 
                                          boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}