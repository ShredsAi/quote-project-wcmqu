package ai.shreds.adapter.primary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ai.shreds.application.services.ApplicationApprovalService;
import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.dtos.SharedApprovalAuditLogDTO;
import ai.shreds.shared.dtos.SharedApprovalQueueDTO;
import ai.shreds.shared.value_objects.SharedPendingRequestsQueryParams;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;
import ai.shreds.shared.value_objects.SharedQueuesQueryParams;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller exposing approval workflow endpoints for moderators.
 */
@RestController
@RequestMapping("/api/approval")
@Validated
public class AdapterApprovalController {

    private final ApplicationApprovalService approvalService;

    public AdapterApprovalController(ApplicationApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * Retrieves pending approval requests with optional filters.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<SharedApprovalRequestDTO>> getPendingRequests(
            @RequestParam(required = false) String moderatorId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String queue) {
        
        SharedPendingRequestsQueryParams queryParams = new SharedPendingRequestsQueryParams(moderatorId, priority, queue);
        List<SharedApprovalRequestDTO> requests = approvalService.retrievePendingRequests(queryParams);
        return ResponseEntity.ok(requests);
    }

    /**
     * Approves a quote by request ID.
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<SharedApprovalDecisionDTO> approveQuote(
            @PathVariable String requestId,
            @Valid @RequestBody SharedApprovalDecisionRequestParams params) {
        
        SharedApprovalDecisionDTO decision = approvalService.approveQuote(requestId, params);
        return ResponseEntity.ok(decision);
    }

    /**
     * Rejects a quote by request ID.
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<SharedApprovalDecisionDTO> rejectQuote(
            @PathVariable String requestId,
            @Valid @RequestBody SharedApprovalDecisionRequestParams params) {
        
        SharedApprovalDecisionDTO decision = approvalService.rejectQuote(requestId, params);
        return ResponseEntity.ok(decision);
    }

    /**
     * Assigns a moderator to an approval request.
     */
    @PutMapping("/{requestId}/assign")
    public ResponseEntity<SharedApprovalRequestDTO> assignModerator(
            @PathVariable String requestId,
            @Valid @RequestBody SharedModeratorAssignmentRequestParams params) {
        
        SharedApprovalRequestDTO request = approvalService.assignModerator(requestId, params);
        return ResponseEntity.ok(request);
    }

    /**
     * Retrieves the audit trail for a given approval request.
     */
    @GetMapping("/audit/{requestId}")
    public ResponseEntity<List<SharedApprovalAuditLogDTO>> getAuditTrail(@PathVariable String requestId) {
        List<SharedApprovalAuditLogDTO> auditLogs = approvalService.retrieveAuditTrail(requestId);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Retrieves approval queues with optional filters.
     */
    @GetMapping("/queues")
    public ResponseEntity<List<SharedApprovalQueueDTO>> getQueues(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String moderatorId) {
        
        SharedQueuesQueryParams queryParams = new SharedQueuesQueryParams(active, moderatorId);
        List<SharedApprovalQueueDTO> queues = approvalService.retrieveQueues(queryParams);
        return ResponseEntity.ok(queues);
    }
}