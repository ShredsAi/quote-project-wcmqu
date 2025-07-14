package ai.shreds.adapter.primary;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import ai.shreds.application.ports.ApplicationInputPortSubmitForApproval;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;

/**
 * Adapter listening for internal QuoteCreatedEvent and forwarding to the application core.
 */
@Component
public class AdapterQuoteEventListener {

    private final ApplicationInputPortSubmitForApproval submitForApprovalPort;

    public AdapterQuoteEventListener(ApplicationInputPortSubmitForApproval submitForApprovalPort) {
        this.submitForApprovalPort = submitForApprovalPort;
    }

    @EventListener
    public void onQuoteCreated(SharedQuoteCreatedEventDTO event) {
        submitForApprovalPort.submitForApproval(event);
    }
}
