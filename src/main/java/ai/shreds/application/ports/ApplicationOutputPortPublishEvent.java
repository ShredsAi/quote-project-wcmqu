package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;

/**
 * Output port for publishing approval workflow events.
 */
public interface ApplicationOutputPortPublishEvent {

    /**
     * Publishes a quote approved event.
     *
     * @param event the quote approved event details
     */
    void publishQuoteApproved(SharedQuoteApprovedEventDTO event);

    /**
     * Publishes a quote rejected event.
     *
     * @param event the quote rejected event details
     */
    void publishQuoteRejected(SharedQuoteRejectedEventDTO event);
}