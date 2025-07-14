package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.exceptions.DomainInvalidStatusTransitionException;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainDecisionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for handling approval and rejection decisions.
 * This service contains the core business logic for processing moderator decisions.
 */
@Slf4j
@RequiredArgsConstructor
public class DomainApprovalDecisionService {
    
    private final DomainOutputPortApprovalDecisionRepository decisionRepository;
    private final DomainOutputPortApprovalRequestRepository requestRepository;
    private final DomainOutputPortAuditLogRepository auditLogRepository;
    
    /**
     * Processes a quote approval decision.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator making the decision
     * @param comments optional comments from the moderator
     * @return the created approval decision entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainInvalidStatusTransitionException if the request cannot be approved
     */
    public DomainApprovalDecisionEntity approveQuote(String requestId, String moderatorId, String comments) {
        validateDecisionParameters(requestId, moderatorId);
        
        // Get the approval request
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        // Validate that the moderator can make this decision
        request.validateModeratorAuthorization(moderatorId);
        
        // Validate that the request is in a state that allows approval
        if (!request.getStatus().isInProgress()) {
            throw new DomainInvalidStatusTransitionException(
                "Request is not in a state that allows approval: " + request.getStatus());
        }
        
        // Create the approval decision
        DomainApprovalDecisionEntity decision = DomainApprovalDecisionEntity.builder()
                .decisionId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .moderatorId(moderatorId)
                .decision(DomainDecisionType.APPROVED)
                .reason("Approved by moderator")
                .comments(comments)
                .build();
        
        // Initialize the decision with current timestamp
        decision.initializeDecision();
        
        // Calculate processing time
        decision.calculateProcessingTime(request.getAssignedAt());
        
        // Validate the decision
        decision.validate();
        
        // Save the decision
        DomainApprovalDecisionEntity savedDecision = decisionRepository.save(decision);
        
        // Update the request status to APPROVED
        request.updateStatus(DomainApprovalStatus.APPROVED);
        requestRepository.update(request);
        
        // Create audit log entry
        createDecisionAuditLog(requestId, moderatorId, "APPROVED", "Approved by moderator", comments);
        
        log.info("Quote approved for request {} by moderator {} in {}ms", 
                requestId, moderatorId, decision.getProcessingTimeMs());
        
        return savedDecision;
    }
    
    /**
     * Processes a quote rejection decision.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator making the decision
     * @param reason the reason for rejection
     * @param comments optional additional comments from the moderator
     * @return the created rejection decision entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainInvalidStatusTransitionException if the request cannot be rejected
     */
    public DomainApprovalDecisionEntity rejectQuote(String requestId, String moderatorId, String reason, String comments) {
        validateDecisionParameters(requestId, moderatorId);
        validateRejectionReason(reason);
        
        // Get the approval request
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        // Validate that the moderator can make this decision
        request.validateModeratorAuthorization(moderatorId);
        
        // Validate that the request is in a state that allows rejection
        if (!request.getStatus().isInProgress()) {
            throw new DomainInvalidStatusTransitionException(
                "Request is not in a state that allows rejection: " + request.getStatus());
        }
        
        // Create the rejection decision
        DomainApprovalDecisionEntity decision = DomainApprovalDecisionEntity.builder()
                .decisionId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .moderatorId(moderatorId)
                .decision(DomainDecisionType.REJECTED)
                .reason(reason)
                .comments(comments)
                .build();
        
        // Initialize the decision with current timestamp
        decision.initializeDecision();
        
        // Calculate processing time
        decision.calculateProcessingTime(request.getAssignedAt());
        
        // Validate the decision
        decision.validate();
        
        // Save the decision
        DomainApprovalDecisionEntity savedDecision = decisionRepository.save(decision);
        
        // Update the request status to REJECTED
        request.updateStatus(DomainApprovalStatus.REJECTED);
        requestRepository.update(request);
        
        // Create audit log entry
        createDecisionAuditLog(requestId, moderatorId, "REJECTED", reason, comments);
        
        log.info("Quote rejected for request {} by moderator {} with reason: {} in {}ms", 
                requestId, moderatorId, reason, decision.getProcessingTimeMs());
        
        return savedDecision;
    }
    
    /**
     * Processes a decision that requires revision.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator making the decision
     * @param reason the reason for requiring revision
     * @param comments optional additional comments from the moderator
     * @return the created revision decision entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainInvalidStatusTransitionException if the request cannot be set to needs revision
     */
    public DomainApprovalDecisionEntity requireRevision(String requestId, String moderatorId, String reason, String comments) {
        validateDecisionParameters(requestId, moderatorId);
        validateRejectionReason(reason);
        
        // Get the approval request
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        // Validate that the moderator can make this decision
        request.validateModeratorAuthorization(moderatorId);
        
        // Validate that the request is in a state that allows revision request
        if (!request.getStatus().isInProgress()) {
            throw new DomainInvalidStatusTransitionException(
                "Request is not in a state that allows revision request: " + request.getStatus());
        }
        
        // Create the revision decision
        DomainApprovalDecisionEntity decision = DomainApprovalDecisionEntity.builder()
                .decisionId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .moderatorId(moderatorId)
                .decision(DomainDecisionType.NEEDS_REVISION)
                .reason(reason)
                .comments(comments)
                .build();
        
        // Initialize the decision with current timestamp
        decision.initializeDecision();
        
        // Calculate processing time
        decision.calculateProcessingTime(request.getAssignedAt());
        
        // Validate the decision
        decision.validate();
        
        // Save the decision
        DomainApprovalDecisionEntity savedDecision = decisionRepository.save(decision);
        
        // Update the request status back to PENDING for revision
        request.updateStatus(DomainApprovalStatus.PENDING);
        // Clear the assigned moderator so it can be reassigned
        request.setAssignedModeratorId(null);
        request.setAssignedAt(null);
        requestRepository.update(request);
        
        // Create audit log entry
        createDecisionAuditLog(requestId, moderatorId, "NEEDS_REVISION", reason, comments);
        
        log.info("Quote revision required for request {} by moderator {} with reason: {} in {}ms", 
                requestId, moderatorId, reason, decision.getProcessingTimeMs());
        
        return savedDecision;
    }
    
    /**
     * Calculates the processing time between assignment and decision.
     * 
     * @param startTime the time when the request was assigned
     * @param endTime the time when the decision was made
     * @return the processing time in milliseconds
     */
    public Long calculateProcessingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return ChronoUnit.MILLIS.between(startTime, endTime);
    }
    
    /**
     * Retrieves all decisions for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return a list of decisions for the request
     * @throws IllegalArgumentException if requestId is invalid
     */
    public List<DomainApprovalDecisionEntity> getDecisionsForRequest(String requestId) {
        validateRequestId(requestId);
        return decisionRepository.findByRequestId(requestId);
    }
    
    /**
     * Retrieves the latest decision for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return the latest decision for the request if found
     * @throws IllegalArgumentException if requestId is invalid
     */
    public Optional<DomainApprovalDecisionEntity> getLatestDecisionForRequest(String requestId) {
        validateRequestId(requestId);
        return decisionRepository.findLatestByRequestId(requestId);
    }
    
    /**
     * Checks if a decision has already been made for a request.
     * 
     * @param requestId the ID of the approval request
     * @return true if a decision exists for the request
     * @throws IllegalArgumentException if requestId is invalid
     */
    public boolean hasDecisionForRequest(String requestId) {
        validateRequestId(requestId);
        return decisionRepository.existsByRequestId(requestId);
    }
    
    /**
     * Retrieves an approval request by its ID.
     * 
     * @param requestId the ID of the approval request
     * @return the approval request entity
     * @throws IllegalArgumentException if the request is not found
     */
    private DomainApprovalRequestEntity getApprovalRequestById(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + requestId));
    }
    
    /**
     * Creates an audit log entry for a decision.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator who made the decision
     * @param decision the decision made
     * @param reason the reason for the decision
     * @param comments additional comments
     */
    private void createDecisionAuditLog(String requestId, String moderatorId, String decision, String reason, String comments) {
        DomainApprovalAuditLogEntity auditLog = DomainApprovalAuditLogEntity.createDecisionEntry(
                requestId, moderatorId, decision, reason, null, null);
        auditLogRepository.save(auditLog);
    }
    
    // Validation methods
    private void validateDecisionParameters(String requestId, String moderatorId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
    }
    
    private void validateRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
    }
    
    private void validateRejectionReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty for rejection/revision");
        }
    }
}