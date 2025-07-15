package ai.shreds.domain.value_objects;

/**
 * Enum representing the priority level of an approval request.
 * This value object encapsulates the different priority levels with their respective weights.
 */
public enum DomainPriority {
    /**
     * Low priority requests - standard processing time.
     */
    LOW(1),
    
    /**
     * Normal priority requests - default priority level.
     */
    NORMAL(2),
    
    /**
     * High priority requests - expedited processing.
     */
    HIGH(3),
    
    /**
     * Urgent priority requests - immediate attention required.
     */
    URGENT(4);
    
    private final int weight;
    
    DomainPriority(int weight) {
        this.weight = weight;
    }
    
    /**
     * Gets the numeric weight of this priority level.
     * Higher weight indicates higher priority.
     * 
     * @return the weight value
     */
    public int getWeight() {
        return weight;
    }
    
    /**
     * Checks if this priority is higher than the given priority.
     * 
     * @param other the priority to compare with
     * @return true if this priority is higher than the other
     */
    public boolean isHigherThan(DomainPriority other) {
        return this.weight > other.weight;
    }
    
    /**
     * Checks if this priority is lower than the given priority.
     * 
     * @param other the priority to compare with
     * @return true if this priority is lower than the other
     */
    public boolean isLowerThan(DomainPriority other) {
        return this.weight < other.weight;
    }
    
    /**
     * Returns the deadline in hours based on priority level.
     * 
     * @return deadline in hours
     */
    public int getDeadlineHours() {
        return switch (this) {
            case LOW -> 72;      // 3 days
            case NORMAL -> 48;   // 2 days
            case HIGH -> 24;     // 1 day
            case URGENT -> 4;    // 4 hours
        };
    }
    
    /**
     * Returns true if this priority requires immediate attention.
     * 
     * @return true if this is an urgent priority
     */
    public boolean isUrgent() {
        return this == URGENT;
    }
}