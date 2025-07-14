package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;

/**
 * Input port for approving quotes.
 */
public interface ApplicationInputPortApproveQuote {

    /**
     * Approves a quote based on the approval request.
     *
     * @param requestId the ID of the approval request
     * @param params the approval decision parameters including moderator ID and comments
     * @return the approval decision DTO
     */
    SharedApprovalDecisionDTO approveQuote(String requestId, SharedApprovalDecisionRequestParams params);
}