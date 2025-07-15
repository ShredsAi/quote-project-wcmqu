package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.exceptions.DomainInvalidStatusTransitionException;
import ai.shreds.domain.exceptions.DomainModeratorNotAuthorizedException;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for managing approval request lifecycle.
 * This service contains the core business logic for approval requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainApprovalRequestService {
    
    private final DomainOutputPortApprovalRequestRepository approvalRequestRepository;
    private final DomainOutputPortApprovalQueueRepository queueRepository;
    private final DomainOutputPortAuditLogRepository auditLogRepository;
    
    /**
     * Creates a new approval request for a quote.
     * 
     * @param quoteId the ID of the quote to be approved
     * @param submittedById the ID of the user who submitted the quote
     * @param priority the priority level for the approval request
     * @return the created approval request entity
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public DomainApprovalRequestEntity createApprovalRequest(
            String quoteId, 
            String submittedById, 
            String priority) {
        
        validateCreateApprovalRequestParameters(quoteId, submittedById, priority);
        
        // Check if an approval request already exists for this quote
        if (approvalRequestRepository.existsByQuoteId(quoteId)) {
            throw new IllegalArgumentException("An approval request already exists for quote: " + quoteId);
        }
        
        DomainPriority domainPriority = DomainPriority.valueOf(priority.toUpperCase());
        
        // Find the most appropriate queue for this request
        String queueId = selectQueueForRequest(domainPriority);
        
        // Create the approval request entity
        DomainApprovalRequestEntity request = DomainApprovalRequestEntity.builder()
                .approvalRequestId(UUID.randomUUID().toString())
                .quoteId(quoteId)
                .submittedById(submittedById)
                .priority(domainPriority)
                .status(DomainApprovalStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .queueId(queueId)
                .build();
        
        // Calculate deadline based on priority
        request.calculateDeadline();
        
        // Save the request
        DomainApprovalRequestEntity savedRequest = approvalRequestRepository.save(request);
        
        // Update queue size
        updateQueueSize(queueId, true);
        
        // Create audit log entry
        createAuditLogEntry(savedRequest.getApprovalRequestId(), 
                          submittedById, 
                          "CREATED", 
                          null, 
                          savedRequest.getStatus().toString());
        
        log.info("Created approval request {} for quote {} with priority {}", 
                savedRequest.getApprovalRequestId(), quoteId, priority);
        
        return savedRequest;
    }
    
    /**
     * Assigns a moderator to an approval request.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator to assign
     * @return the updated approval request entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainInvalidStatusTransitionException if the request cannot be assigned
     */
    public DomainApprovalRequestEntity assignModerator(String requestId, String moderatorId) {
        validateAssignModeratorParameters(requestId, moderatorId);
        
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        String oldModeratorId = request.getAssignedModeratorId();
        
        // Assign the moderator (this will update status to IN_REVIEW)
        request.assignModerator(moderatorId);
        
        // Save the updated request
        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.update(request);
        
        // Create audit log entry
        createAuditLogEntry(requestId, 
                          moderatorId, 
                          "ASSIGNED", 
                          oldModeratorId, 
                          moderatorId);
        
        log.info("Assigned moderator {} to approval request {}", moderatorId, requestId);
        
        return updatedRequest;
    }
    
    /**
     * Updates the status of an approval request.
     * 
     * @param requestId the ID of the approval request
     * @param newStatus the new status to set
     * @return the updated approval request entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainInvalidStatusTransitionException if the status transition is not allowed
     */
    public DomainApprovalRequestEntity updateRequestStatus(String requestId, DomainApprovalStatus newStatus) {
        validateUpdateStatusParameters(requestId, newStatus);
        
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        DomainApprovalStatus oldStatus = request.getStatus();
        
        // Update the status (this will validate the transition)
        request.updateStatus(newStatus);
        
        // Save the updated request
        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.update(request);
        
        // If the status is final, update queue size
        if (newStatus.isFinal()) {
            updateQueueSize(request.getQueueId(), false);
        }
        
        // Create audit log entry
        createAuditLogEntry(requestId, 
                          request.getAssignedModeratorId(), 
                          "STATUS_UPDATED", 
                          oldStatus.toString(), 
                          newStatus.toString());
        
        log.info("Updated status of approval request {} from {} to {}", 
                requestId, oldStatus, newStatus);
        
        return updatedRequest;
    }
    
    /**
     * Validates if a decision can be made on an approval request by a moderator.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the moderator making the decision
     * @return true if the decision is valid
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DomainModeratorNotAuthorizedException if the moderator is not authorized
     */
    public boolean validateDecision(String requestId, String moderatorId) {
        validateDecisionParameters(requestId, moderatorId);
        
        DomainApprovalRequestEntity request = getApprovalRequestById(requestId);
        
        // Validate moderator authorization
        request.validateModeratorAuthorization(moderatorId);
        
        // Check if the request is in a state that allows decisions
        if (!request.getStatus().isInProgress()) {
            throw new DomainInvalidStatusTransitionException(
                "Request is not in a state that allows decisions: " + request.getStatus());
        }
        
        return true;
    }
    
    /**
     * Finds an approval request by its ID.
     * 
     * @param requestId the ID of the approval request
     * @return the approval request entity
     * @throws IllegalArgumentException if the request is not found
     */
    public DomainApprovalRequestEntity getApprovalRequestById(String requestId) {
        return approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + requestId));
    }
    
    /**
     * Selects the most appropriate queue for a new approval request.
     * 
     * @param priority the priority of the request
     * @return the queue ID to use
     */
    private String selectQueueForRequest(DomainPriority priority) {
        log.debug("Selecting queue for request with priority: {}", priority);
        
        // Get all active queues
        List<DomainApprovalQueueEntity> allQueues = queueRepository.findActiveQueues();
        
        if (allQueues.isEmpty()) {
            throw new IllegalStateException("No active queues found for approval request");
        }
        
        // For urgent requests, try to find a queue with 'Urgent' in its name
        if (priority.isUrgent()) {
            for (DomainApprovalQueueEntity queue : allQueues) {
                if (queue.getQueueName().toLowerCase().contains("urgent") && queue.canAcceptRequest()) {
                    log.debug("Selected urgent queue: {}", queue.getQueueName());
                    return queue.getQueueId();
                }
            }
        }
        
        // For high priority requests, try to find a queue with 'High' or 'Priority' in its name
        if (priority == DomainPriority.HIGH) {
            for (DomainApprovalQueueEntity queue : allQueues) {
                String queueNameLower = queue.getQueueName().toLowerCase();
                if ((queueNameLower.contains("high") || queueNameLower.contains("priority")) && queue.canAcceptRequest()) {
                    log.debug("Selected high priority queue: {}", queue.getQueueName());
                    return queue.getQueueId();
                }
            }
        }
        
        // For all other cases, try to find a queue with 'General' in its name
        for (DomainApprovalQueueEntity queue : allQueues) {
            if (queue.getQueueName().toLowerCase().contains("general") && queue.canAcceptRequest()) {
                log.debug("Selected general queue: {}", queue.getQueueName());
                return queue.getQueueId();
            }
        }
        
        // If no specific queue is available, use the first available queue
        for (DomainApprovalQueueEntity queue : allQueues) {
            if (queue.canAcceptRequest()) {
                log.debug("Selected first available queue: {}", queue.getQueueName());
                return queue.getQueueId();
            }
        }
        
        // If no queue has capacity, use the least loaded queue
        Optional<DomainApprovalQueueEntity> leastLoadedQueue = queueRepository.findLeastLoadedQueue();
        if (leastLoadedQueue.isPresent()) {
            log.debug("Selected least loaded queue: {}", leastLoadedQueue.get().getQueueName());
            return leastLoadedQueue.get().getQueueId();
        }
        
        // As a last resort, use the first queue
        DomainApprovalQueueEntity firstQueue = allQueues.get(0);
        log.debug("Selected first queue as fallback: {}", firstQueue.getQueueName());
        return firstQueue.getQueueId();
    }
    
    /**
     * Updates the queue size when a request is added or removed.
     * 
     * @param queueId the ID of the queue
     * @param isAdding true if adding a request, false if removing
     */
    private void updateQueueSize(String queueId, boolean isAdding) {
        if (queueId == null) {
            return;
        }
        
        Optional<DomainApprovalQueueEntity> queueOpt = queueRepository.findById(queueId);
        if (queueOpt.isPresent()) {
            DomainApprovalQueueEntity queue = queueOpt.get();
            if (isAdding) {
                queue.incrementSize();
            } else {
                queue.decrementSize();
            }
            queueRepository.update(queue);
        }
    }
    
    /**
     * Creates an audit log entry for an action.
     * 
     * @param requestId the ID of the approval request
     * @param performedById the ID of the user who performed the action
     * @param action the action performed
     * @param oldValue the old value (if applicable)
     * @param newValue the new value (if applicable)
     */
    private void createAuditLogEntry(String requestId, String performedById, String action, String oldValue, String newValue) {
        DomainApprovalAuditLogEntity auditLog = DomainApprovalAuditLogEntity.builder()
                .auditId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .action(action)
                .performedById(performedById)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(auditLog);
    }
    
    // Validation methods
    private void validateCreateApprovalRequestParameters(String quoteId, String submittedById, String priority) {
        if (quoteId == null || quoteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        if (submittedById == null || submittedById.trim().isEmpty()) {
            throw new IllegalArgumentException("Submitted by ID cannot be null or empty");
        }
        if (priority == null || priority.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority cannot be null or empty");
        }
        try {
            DomainPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }
    
    private void validateAssignModeratorParameters(String requestId, String moderatorId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
    }
    
    private void validateUpdateStatusParameters(String requestId, DomainApprovalStatus newStatus) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
    }
    
    private void validateDecisionParameters(String requestId, String moderatorId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
    }
}