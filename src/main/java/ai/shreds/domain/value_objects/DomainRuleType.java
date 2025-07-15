package ai.shreds.domain.value_objects;

/**
 * Enum representing the type of moderation rule.
 * This value object encapsulates the different kinds of rules that can be applied during the moderation process.
 */
public enum DomainRuleType {
    /**
     * Rules that validate the content of a quote against specific criteria such as 
     * profanity checks, length requirements, and allowed content.
     */
    CONTENT_VALIDATION,
    
    /**
     * Rules that check if a quote is duplicated or too similar to existing quotes.
     */
    DUPLICATE_CHECK;
    
    /**
     * Determines if this rule type requires access to existing content database.
     * 
     * @return true if this rule type requires content database access
     */
    public boolean requiresContentDatabaseAccess() {
        return this == DUPLICATE_CHECK;
    }
    
    /**
     * Returns true if this rule type can be executed in parallel with other rules.
     * 
     * @return true if the rule can be parallelized
     */
    public boolean canBeParallelized() {
        // CONTENT_VALIDATION rules can typically be run in parallel
        // DUPLICATE_CHECK might require exclusive database access
        return this == CONTENT_VALIDATION;
    }
    
    /**
     * Returns the recommended timeout in milliseconds for rule execution.
     * 
     * @return the recommended timeout in milliseconds
     */
    public long getRecommendedTimeoutMs() {
        return switch (this) {
            case CONTENT_VALIDATION -> 2000;  // 2 seconds
            case DUPLICATE_CHECK -> 5000;     // 5 seconds
        };
    }
}