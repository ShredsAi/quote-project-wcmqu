package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;

/**
 * Input port for rejecting quotes.
 */
public interface ApplicationInputPortRejectQuote {

    /**
     * Rejects a quote based on the approval request.
     *
     * @param requestId the ID of the approval request
     * @param params the rejection decision parameters including moderator ID, reason, and comments
     * @return the rejection decision DTO
     */
    SharedApprovalDecisionDTO rejectQuote(String requestId, SharedApprovalDecisionRequestParams params);
}