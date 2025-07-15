package ai.shreds.domain.value_objects;

/**
 * Enum representing the status of an approval request in the workflow.
 * This value object encapsulates the different states an approval request can be in.
 */
public enum DomainApprovalStatus {
    /**
     * The request has been submitted but not yet assigned to a moderator.
     */
    PENDING,
    
    /**
     * The request has been assigned to a moderator and is currently under review.
     */
    IN_REVIEW,
    
    /**
     * The request has been approved by a moderator.
     */
    APPROVED,
    
    /**
     * The request has been rejected by a moderator.
     */
    REJECTED;
    
    /**
     * Checks if the current status allows transition to the target status.
     * 
     * @param targetStatus the status to transition to
     * @return true if the transition is allowed, false otherwise
     */
    public boolean canTransitionTo(DomainApprovalStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == IN_REVIEW;
            case IN_REVIEW -> targetStatus == APPROVED || targetStatus == REJECTED;
            case APPROVED, REJECTED -> false; // Final states
        };
    }
    
    /**
     * Returns true if this status represents a final state (cannot transition further).
     * 
     * @return true if this is a final status
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
    
    /**
     * Returns true if this status indicates the request is actively being processed.
     * 
     * @return true if the request is in an active processing state
     */
    public boolean isInProgress() {
        return this == IN_REVIEW;
    }
}