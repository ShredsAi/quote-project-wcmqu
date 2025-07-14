package ai.shreds.domain.value_objects;

/**
 * Enum representing the type of decision made on an approval request.
 * This value object encapsulates the different decisions a moderator can make.
 */
public enum DomainDecisionType {
    /**
     * The request has been approved by the moderator.
     * This decision results in the quote being published.
     */
    APPROVED,
    
    /**
     * The request has been rejected by the moderator.
     * This decision prevents the quote from being published.
     */
    REJECTED,
    
    /**
     * The request requires revisions before it can be approved.
     * This decision requests changes from the submitter.
     */
    NEEDS_REVISION;
    
    /**
     * Returns the corresponding approval status for this decision type.
     * 
     * @return the approval status that results from this decision
     */
    public DomainApprovalStatus toApprovalStatus() {
        return switch (this) {
            case APPROVED -> DomainApprovalStatus.APPROVED;
            case REJECTED -> DomainApprovalStatus.REJECTED;
            case NEEDS_REVISION -> DomainApprovalStatus.PENDING; // Goes back to pending for revision
        };
    }
    
    /**
     * Returns true if this decision is final (no further actions needed).
     * 
     * @return true if this decision is final
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
    
    /**
     * Returns true if this decision requires a reason to be provided.
     * 
     * @return true if a reason is required for this decision
     */
    public boolean requiresReason() {
        return this == REJECTED || this == NEEDS_REVISION;
    }
}