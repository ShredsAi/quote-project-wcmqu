package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;

/**
 * Input port for submitting quotes for approval based on a created event.
 */
public interface ApplicationInputPortSubmitForApproval {

    /**
     * Submits a quote for approval when a new quote is created.
     *
     * @param quoteEvent the event containing quote submission details
     * @return the created approval request DTO
     */
    SharedApprovalRequestDTO submitForApproval(SharedQuoteCreatedEventDTO quoteEvent);
}