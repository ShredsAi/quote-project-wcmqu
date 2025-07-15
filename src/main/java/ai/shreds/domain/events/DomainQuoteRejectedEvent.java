package ai.shreds.domain.events;

import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain event representing a quote rejection.
 * This event is published when a quote is rejected by a moderator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainQuoteRejectedEvent {
    
    private String quoteId;
    private String moderatorId;
    private LocalDateTime timestamp;
    private String reason;
    
    private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Creates a new quote rejected event with the current timestamp.
     * 
     * @param quoteId the ID of the rejected quote
     * @param moderatorId the ID of the moderator who rejected the quote
     * @param reason the reason for rejection
     * @return a new DomainQuoteRejectedEvent instance
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public static DomainQuoteRejectedEvent create(String quoteId, String moderatorId, String reason) {
        validateParameters(quoteId, moderatorId, reason);
        
        return DomainQuoteRejectedEvent.builder()
                .quoteId(quoteId)
                .moderatorId(moderatorId)
                .timestamp(LocalDateTime.now())
                .reason(reason)
                .build();
    }
    
    /**
     * Validates the event data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the event data is invalid
     */
    public void validate() {
        validateParameters(quoteId, moderatorId, reason);
        
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        
        // Ensure timestamp is not in the future
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event timestamp cannot be in the future");
        }
    }
    
    /**
     * Converts this domain event to a DTO for external communication.
     * 
     * @return a SharedQuoteRejectedEventDTO representing this event
     */
    public SharedQuoteRejectedEventDTO toDTO() {
        return SharedQuoteRejectedEventDTO.builder()
                .eventType("QuoteRejectedEvent")
                .quoteId(quoteId)
                .moderatorId(moderatorId)
                .timestamp(timestamp != null ? timestamp.format(EVENT_DATE_FORMAT) : null)
                .reason(reason)
                .build();
    }
    
    /**
     * Creates a domain event from a DTO.
     * 
     * @param dto the DTO to convert from
     * @return a new DomainQuoteRejectedEvent instance
     * @throws IllegalArgumentException if the DTO is null or invalid
     */
    public static DomainQuoteRejectedEvent fromDTO(SharedQuoteRejectedEventDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        
        return DomainQuoteRejectedEvent.builder()
                .quoteId(dto.getQuoteId())
                .moderatorId(dto.getModeratorId())
                .timestamp(dto.getTimestamp() != null ? 
                    LocalDateTime.parse(dto.getTimestamp(), EVENT_DATE_FORMAT) : null)
                .reason(dto.getReason())
                .build();
    }
    
    /**
     * Gets the event type identifier.
     * 
     * @return the event type string
     */
    public String getEventType() {
        return "QuoteRejectedEvent";
    }
    
    /**
     * Gets a formatted timestamp string.
     * 
     * @return the formatted timestamp
     */
    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(EVENT_DATE_FORMAT) : null;
    }
    
    /**
     * Checks if this event occurred within the specified time window.
     * 
     * @param windowMinutes the time window in minutes
     * @return true if the event occurred within the time window
     */
    public boolean isWithinTimeWindow(int windowMinutes) {
        if (timestamp == null || windowMinutes <= 0) {
            return false;
        }
        
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);
        return timestamp.isAfter(windowStart);
    }
    
    /**
     * Creates a copy of this event with a new timestamp.
     * 
     * @param newTimestamp the new timestamp to use
     * @return a new DomainQuoteRejectedEvent instance with the updated timestamp
     * @throws IllegalArgumentException if the new timestamp is null
     */
    public DomainQuoteRejectedEvent withTimestamp(LocalDateTime newTimestamp) {
        if (newTimestamp == null) {
            throw new IllegalArgumentException("New timestamp cannot be null");
        }
        
        return DomainQuoteRejectedEvent.builder()
                .quoteId(this.quoteId)
                .moderatorId(this.moderatorId)
                .timestamp(newTimestamp)
                .reason(this.reason)
                .build();
    }
    
    /**
     * Creates a copy of this event with a new reason.
     * 
     * @param newReason the new reason to use
     * @return a new DomainQuoteRejectedEvent instance with the updated reason
     * @throws IllegalArgumentException if the new reason is null or empty
     */
    public DomainQuoteRejectedEvent withReason(String newReason) {
        if (newReason == null || newReason.trim().isEmpty()) {
            throw new IllegalArgumentException("New reason cannot be null or empty");
        }
        
        return DomainQuoteRejectedEvent.builder()
                .quoteId(this.quoteId)
                .moderatorId(this.moderatorId)
                .timestamp(this.timestamp)
                .reason(newReason)
                .build();
    }
    
    /**
     * Checks if this rejection is due to a critical rule violation.
     * 
     * @return true if the reason indicates a critical rule violation
     */
    public boolean isCriticalRuleViolation() {
        return reason != null && 
               (reason.toLowerCase().contains("critical") || 
                reason.toLowerCase().contains("violation") ||
                reason.toLowerCase().contains("inappropriate") ||
                reason.toLowerCase().contains("policy"));
    }
    
    /**
     * Gets the rejection category based on the reason.
     * 
     * @return the rejection category
     */
    public String getRejectionCategory() {
        if (reason == null) {
            return "UNKNOWN";
        }
        
        String lowerReason = reason.toLowerCase();
        
        if (lowerReason.contains("inappropriate") || lowerReason.contains("offensive")) {
            return "CONTENT_VIOLATION";
        } else if (lowerReason.contains("duplicate")) {
            return "DUPLICATE_CONTENT";
        } else if (lowerReason.contains("attribution") || lowerReason.contains("source")) {
            return "ATTRIBUTION_ISSUE";
        } else if (lowerReason.contains("quality") || lowerReason.contains("standard")) {
            return "QUALITY_ISSUE";
        } else if (lowerReason.contains("length") || lowerReason.contains("format")) {
            return "FORMAT_ISSUE";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * Returns a string representation of this event.
     * 
     * @return a string representation including key event details
     */
    @Override
    public String toString() {
        return String.format("QuoteRejectedEvent{quoteId='%s', moderatorId='%s', timestamp=%s, reason='%s'}", 
                quoteId, moderatorId, getFormattedTimestamp(), reason);
    }
    
    /**
     * Validates the required parameters for the event.
     * 
     * @param quoteId the quote ID to validate
     * @param moderatorId the moderator ID to validate
     * @param reason the reason to validate
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    private static void validateParameters(String quoteId, String moderatorId, String reason) {
        if (quoteId == null || quoteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
    }
}