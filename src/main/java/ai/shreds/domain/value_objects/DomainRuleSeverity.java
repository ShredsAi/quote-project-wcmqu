package ai.shreds.domain.value_objects;

/**
 * Enum representing the severity of a moderation rule violation.
 * This value object encapsulates the different levels of severity for rule violations.
 */
public enum DomainRuleSeverity {
    /**
     * Informational severity - violations don't block approval but should be noted.
     */
    INFO(1, false),
    
    /**
     * Warning severity - violations should be carefully reviewed but don't automatically block approval.
     */
    WARNING(2, false),
    
    /**
     * Critical severity - violations automatically block approval and require resolution.
     */
    CRITICAL(3, true);
    
    private final int level;
    private final boolean isBlocking;
    
    DomainRuleSeverity(int level, boolean isBlocking) {
        this.level = level;
        this.isBlocking = isBlocking;
    }
    
    /**
     * Gets the numeric level of this severity.
     * Higher level indicates higher severity.
     * 
     * @return the numeric severity level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Indicates whether this severity level blocks automatic approval.
     * 
     * @return true if this severity level blocks approval
     */
    public boolean isBlocking() {
        return isBlocking;
    }
    
    /**
     * Checks if this severity is higher than the given severity.
     * 
     * @param other the severity to compare with
     * @return true if this severity is higher than the other
     */
    public boolean isHigherThan(DomainRuleSeverity other) {
        return this.level > other.level;
    }
    
    /**
     * Returns the appropriate action to take for this severity level.
     * 
     * @return a string describing the recommended action
     */
    public String getRecommendedAction() {
        return switch (this) {
            case INFO -> "Note in review comments";
            case WARNING -> "Thoroughly review content";
            case CRITICAL -> "Reject or require revision";
        };
    }
    
    /**
     * Returns true if this severity requires escalation to a senior moderator.
     * 
     * @return true if escalation is recommended
     */
    public boolean requiresEscalation() {
        return this == CRITICAL;
    }
}