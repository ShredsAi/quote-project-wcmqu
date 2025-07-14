package ai.shreds.adapter.primary;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ai.shreds.application.ports.ApplicationOutputPortPublishEvent;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;

/**
 * Adapter publishing quote approval and rejection events via Spring ApplicationEvents
 */
@Component
public class AdapterApprovalEventPublisher implements ApplicationOutputPortPublishEvent {

    private final ApplicationEventPublisher eventPublisher;

    public AdapterApprovalEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishQuoteApproved(SharedQuoteApprovedEventDTO event) {
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishQuoteRejected(SharedQuoteRejectedEventDTO event) {
        eventPublisher.publishEvent(event);
    }
}
