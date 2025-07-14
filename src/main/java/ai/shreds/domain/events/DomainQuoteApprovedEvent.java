package ai.shreds.domain.events;

import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain event representing a quote approval.
 * This event is published when a quote is approved by a moderator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainQuoteApprovedEvent {
    
    private String quoteId;
    private String moderatorId;
    private LocalDateTime timestamp;
    
    private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Creates a new quote approved event with the current timestamp.
     * 
     * @param quoteId the ID of the approved quote
     * @param moderatorId the ID of the moderator who approved the quote
     * @return a new DomainQuoteApprovedEvent instance
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public static DomainQuoteApprovedEvent create(String quoteId, String moderatorId) {
        validateParameters(quoteId, moderatorId);
        
        return DomainQuoteApprovedEvent.builder()
                .quoteId(quoteId)
                .moderatorId(moderatorId)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Validates the event data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the event data is invalid
     */
    public void validate() {
        validateParameters(quoteId, moderatorId);
        
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
     * @return a SharedQuoteApprovedEventDTO representing this event
     */
    public SharedQuoteApprovedEventDTO toDTO() {
        return SharedQuoteApprovedEventDTO.builder()
                .eventType("QuoteApprovedEvent")
                .quoteId(quoteId)
                .moderatorId(moderatorId)
                .timestamp(timestamp != null ? timestamp.format(EVENT_DATE_FORMAT) : null)
                .build();
    }
    
    /**
     * Creates a domain event from a DTO.
     * 
     * @param dto the DTO to convert from
     * @return a new DomainQuoteApprovedEvent instance
     * @throws IllegalArgumentException if the DTO is null or invalid
     */
    public static DomainQuoteApprovedEvent fromDTO(SharedQuoteApprovedEventDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        
        return DomainQuoteApprovedEvent.builder()
                .quoteId(dto.getQuoteId())
                .moderatorId(dto.getModeratorId())
                .timestamp(dto.getTimestamp() != null ? 
                    LocalDateTime.parse(dto.getTimestamp(), EVENT_DATE_FORMAT) : null)
                .build();
    }
    
    /**
     * Gets the event type identifier.
     * 
     * @return the event type string
     */
    public String getEventType() {
        return "QuoteApprovedEvent";
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
     * @return a new DomainQuoteApprovedEvent instance with the updated timestamp
     * @throws IllegalArgumentException if the new timestamp is null
     */
    public DomainQuoteApprovedEvent withTimestamp(LocalDateTime newTimestamp) {
        if (newTimestamp == null) {
            throw new IllegalArgumentException("New timestamp cannot be null");
        }
        
        return DomainQuoteApprovedEvent.builder()
                .quoteId(this.quoteId)
                .moderatorId(this.moderatorId)
                .timestamp(newTimestamp)
                .build();
    }
    
    /**
     * Returns a string representation of this event.
     * 
     * @return a string representation including key event details
     */
    @Override
    public String toString() {
        return String.format("QuoteApprovedEvent{quoteId='%s', moderatorId='%s', timestamp=%s}", 
                quoteId, moderatorId, getFormattedTimestamp());
    }
    
    /**
     * Validates the required parameters for the event.
     * 
     * @param quoteId the quote ID to validate
     * @param moderatorId the moderator ID to validate
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    private static void validateParameters(String quoteId, String moderatorId) {
        if (quoteId == null || quoteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
    }
}