package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalAuditLogDTO;

import java.util.List;

/**
 * Input port for retrieving audit trails for approval requests.
 */
public interface ApplicationInputPortRetrieveAuditTrail {

    /**
     * Retrieves the complete audit trail for a specific approval request.
     *
     * @param requestId the ID of the approval request
     * @return list of audit log entries
     */
    List<SharedApprovalAuditLogDTO> retrieveAuditTrail(String requestId);
}