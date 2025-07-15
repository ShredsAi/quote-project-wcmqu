package ai.shreds.adapter.primary;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import ai.shreds.application.ports.ApplicationInputPortSubmitForApproval;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter listening for internal QuoteCreatedEvent and forwarding to the application core.
 */
@Component
public class AdapterQuoteEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AdapterQuoteEventListener.class);
    private final ApplicationInputPortSubmitForApproval submitForApprovalPort;

    public AdapterQuoteEventListener(ApplicationInputPortSubmitForApproval submitForApprovalPort) {
        this.submitForApprovalPort = submitForApprovalPort;
    }

    @EventListener
    @Async
    public void onQuoteCreated(SharedQuoteCreatedEventDTO event) {
        logger.info("Processing QuoteCreatedEvent for quote ID: {}", event.getQuoteId());
        try {
            submitForApprovalPort.submitForApproval(event);
            logger.info("Successfully processed QuoteCreatedEvent for quote ID: {}", event.getQuoteId());
        } catch (Exception e) {
            logger.error("Failed to process QuoteCreatedEvent for quote ID: {}", event.getQuoteId(), e);
            throw e;
        }
    }
}