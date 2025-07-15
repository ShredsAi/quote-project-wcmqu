package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortPublishEvent;
import ai.shreds.infrastructure.exceptions.InfrastructureMessagingException;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfrastructureEventPublisher implements ApplicationOutputPortPublishEvent {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishQuoteApproved(SharedQuoteApprovedEventDTO event) {
        try {
            log.debug("Publishing quote approved event for quote ID: {}", event.getQuoteId());
            
            // Validate the event
            validateQuoteApprovedEvent(event);
            
            // Publish the event using Spring's ApplicationEventPublisher
            applicationEventPublisher.publishEvent(event);
            
            log.info("Successfully published quote approved event for quote ID: {} by moderator: {}", 
                event.getQuoteId(), event.getModeratorId());
        } catch (Exception e) {
            log.error("Error publishing quote approved event for quote ID: {}", event.getQuoteId(), e);
            throw new InfrastructureMessagingException("Failed to publish quote approved event", e);
        }
    }

    @Override
    public void publishQuoteRejected(SharedQuoteRejectedEventDTO event) {
        try {
            log.debug("Publishing quote rejected event for quote ID: {}", event.getQuoteId());
            
            // Validate the event
            validateQuoteRejectedEvent(event);
            
            // Publish the event using Spring's ApplicationEventPublisher
            applicationEventPublisher.publishEvent(event);
            
            log.info("Successfully published quote rejected event for quote ID: {} by moderator: {} with reason: {}", 
                event.getQuoteId(), event.getModeratorId(), event.getReason());
        } catch (Exception e) {
            log.error("Error publishing quote rejected event for quote ID: {}", event.getQuoteId(), e);
            throw new InfrastructureMessagingException("Failed to publish quote rejected event", e);
        }
    }

    /**
     * Validates the quote approved event before publishing.
     * 
     * @param event the event to validate
     * @throws IllegalArgumentException if the event is invalid
     */
    private void validateQuoteApprovedEvent(SharedQuoteApprovedEventDTO event) {
        if (event == null) {
            throw new IllegalArgumentException("Quote approved event cannot be null");
        }
        
        if (event.getQuoteId() == null || event.getQuoteId().trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        
        if (event.getModeratorId() == null || event.getModeratorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
        
        if (event.getTimestamp() == null || event.getTimestamp().trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
        
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
    }

    /**
     * Validates the quote rejected event before publishing.
     * 
     * @param event the event to validate
     * @throws IllegalArgumentException if the event is invalid
     */
    private void validateQuoteRejectedEvent(SharedQuoteRejectedEventDTO event) {
        if (event == null) {
            throw new IllegalArgumentException("Quote rejected event cannot be null");
        }
        
        if (event.getQuoteId() == null || event.getQuoteId().trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        
        if (event.getModeratorId() == null || event.getModeratorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
        
        if (event.getTimestamp() == null || event.getTimestamp().trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
        
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        
        if (event.getReason() == null || event.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be null or empty");
        }
    }
}