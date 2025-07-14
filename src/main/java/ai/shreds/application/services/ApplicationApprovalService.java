package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationInputPortApproveQuote;
import ai.shreds.application.ports.ApplicationInputPortAssignModerator;
import ai.shreds.application.ports.ApplicationInputPortRejectQuote;
import ai.shreds.application.ports.ApplicationInputPortRetrieveAuditTrail;
import ai.shreds.application.ports.ApplicationInputPortRetrievePendingRequests;
import ai.shreds.application.ports.ApplicationInputPortRetrieveQueues;
import ai.shreds.application.ports.ApplicationInputPortSubmitForApproval;
import ai.shreds.application.ports.ApplicationOutputPortPublishEvent;
import ai.shreds.application.ports.ApplicationOutputPortSendNotification;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedApprovalAuditLogDTO;
import ai.shreds.shared.dtos.SharedApprovalQueueDTO;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;
import ai.shreds.shared.dtos.SharedModerationAssignmentDTO;
import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;
import ai.shreds.shared.value_objects.SharedPendingRequestsQueryParams;
import ai.shreds.shared.value_objects.SharedQueuesQueryParams;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.services.DomainApprovalRequestService;
import ai.shreds.domain.services.DomainApprovalDecisionService;
import ai.shreds.domain.services.DomainBusinessRulesEngine;
import ai.shreds.domain.services.DomainApprovalQueueService;
import ai.shreds.domain.services.DomainAuditTrailService;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.application.exceptions.ApplicationApprovalNotFoundException;
import ai.shreds.application.exceptions.ApplicationInvalidStatusTransitionException;
import ai.shreds.application.exceptions.ApplicationModeratorNotAuthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service implementing all approval workflow use cases.
 * This service orchestrates the approval process by coordinating between domain services
 * and external systems through output ports.
 */
@Service
public class ApplicationApprovalService implements
        ApplicationInputPortSubmitForApproval,
        ApplicationInputPortApproveQuote,
        ApplicationInputPortRejectQuote,
        ApplicationInputPortAssignModerator,
        ApplicationInputPortRetrievePendingRequests,
        ApplicationInputPortRetrieveAuditTrail,
        ApplicationInputPortRetrieveQueues {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationApprovalService.class);

    private final DomainApprovalRequestService domainApprovalRequestService;
    private final DomainApprovalDecisionService domainApprovalDecisionService;
    private final DomainBusinessRulesEngine domainBusinessRulesEngine;
    private final DomainApprovalQueueService domainApprovalQueueService;
    private final DomainAuditTrailService domainAuditTrailService;
    private final DomainOutputPortApprovalRequestRepository requestRepository;
    private final DomainOutputPortApprovalDecisionRepository decisionRepository;
    private final DomainOutputPortApprovalQueueRepository queueRepository;
    private final DomainOutputPortAuditLogRepository auditLogRepository;
    private final ApplicationOutputPortPublishEvent eventPublisher;
    private final ApplicationOutputPortSendNotification notificationSender;

    public ApplicationApprovalService(
            DomainApprovalRequestService domainApprovalRequestService,
            DomainApprovalDecisionService domainApprovalDecisionService,
            DomainBusinessRulesEngine domainBusinessRulesEngine,
            DomainApprovalQueueService domainApprovalQueueService,
            DomainAuditTrailService domainAuditTrailService,
            DomainOutputPortApprovalRequestRepository requestRepository,
            DomainOutputPortApprovalDecisionRepository decisionRepository,
            DomainOutputPortApprovalQueueRepository queueRepository,
            DomainOutputPortAuditLogRepository auditLogRepository,
            ApplicationOutputPortPublishEvent eventPublisher,
            ApplicationOutputPortSendNotification notificationSender) {
        this.domainApprovalRequestService = domainApprovalRequestService;
        this.domainApprovalDecisionService = domainApprovalDecisionService;
        this.domainBusinessRulesEngine = domainBusinessRulesEngine;
        this.domainApprovalQueueService = domainApprovalQueueService;
        this.domainAuditTrailService = domainAuditTrailService;
        this.requestRepository = requestRepository;
        this.decisionRepository = decisionRepository;
        this.queueRepository = queueRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional
    public SharedApprovalRequestDTO submitForApproval(SharedQuoteCreatedEventDTO quoteEvent) {
        logger.info("Submitting quote {} for approval", quoteEvent.getQuoteId());
        
        try {
            // Create approval request using domain service
            DomainApprovalRequestEntity request = domainApprovalRequestService.createApprovalRequest(
                    quoteEvent.getQuoteId(), 
                    quoteEvent.getSubmittedBy(), 
                    quoteEvent.getPriority());
            
            // Execute business rules
            Map<String, Object> ruleResults = domainBusinessRulesEngine.executeRules(request);
            logger.debug("Business rules execution results: {}", ruleResults);
            
            // Add to appropriate queue
            domainApprovalQueueService.addToQueue(request, request.getQueueId());
            
            // Record audit trail
            domainAuditTrailService.recordAction(
                    request.getApprovalRequestId(), 
                    "CREATED", 
                    quoteEvent.getSubmittedBy(), 
                    null, 
                    "Approval request created for quote: " + quoteEvent.getQuoteId());
            
            // Send notification to moderators
            createApprovalNotification("QUOTE_SUBMITTED", null, 
                    "New quote submitted for approval: " + quoteEvent.getQuoteId());
            
            logger.info("Quote {} successfully submitted for approval with request ID {}", 
                    quoteEvent.getQuoteId(), request.getApprovalRequestId());
            
            return request.toDTO();
            
        } catch (Exception e) {
            logger.error("Failed to submit quote {} for approval", quoteEvent.getQuoteId(), e);
            throw new ApplicationApprovalNotFoundException("Failed to submit quote for approval: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SharedApprovalDecisionDTO approveQuote(String requestId, SharedApprovalDecisionRequestParams params) {
        logger.info("Approving quote for request {} by moderator {}", requestId, params.getModeratorId());
        
        try {
            // Validate the request exists and can be processed using domain service
            DomainApprovalRequestEntity request = domainApprovalRequestService.getApprovalRequestById(requestId);
            
            // Validate moderator can process this request
            if (!domainApprovalRequestService.validateDecision(requestId, params.getModeratorId())) {
                throw new ApplicationModeratorNotAuthorizedException(params.getModeratorId(), "approve quote");
            }
            
            // Execute final business rules validation
            Map<String, Object> ruleResults = domainBusinessRulesEngine.executeRules(request);
            logger.debug("Final business rules validation results: {}", ruleResults);
            
            // Process approval decision
            DomainApprovalDecisionEntity decision = domainApprovalDecisionService.approveQuote(
                    requestId, params.getModeratorId(), params.getComments());
            
            // Record audit trail
            domainAuditTrailService.recordAction(
                    requestId, 
                    "APPROVED", 
                    params.getModeratorId(), 
                    "PENDING", 
                    "APPROVED");
            
            // Publish domain event
            SharedQuoteApprovedEventDTO event = SharedQuoteApprovedEventDTO.builder()
                    .eventType("QuoteApprovedEvent")
                    .quoteId(request.getQuoteId())
                    .moderatorId(params.getModeratorId())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
            
            eventPublisher.publishQuoteApproved(event);
            
            // Send approval notification
            createApprovalNotification("QUOTE_APPROVED", request.getSubmittedById(), 
                    "Your quote has been approved by moderator.");
            
            logger.info("Quote approved successfully for request {}", requestId);
            
            return decision.toDTO();
            
        } catch (ApplicationApprovalNotFoundException | ApplicationModeratorNotAuthorizedException e) {
            logger.error("Authorization or validation error while approving quote for request {}", requestId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to approve quote for request {}", requestId, e);
            throw new ApplicationInvalidStatusTransitionException("Failed to approve quote: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SharedApprovalDecisionDTO rejectQuote(String requestId, SharedApprovalDecisionRequestParams params) {
        logger.info("Rejecting quote for request {} by moderator {}", requestId, params.getModeratorId());
        
        try {
            // Validate the request exists and can be processed using domain service
            DomainApprovalRequestEntity request = domainApprovalRequestService.getApprovalRequestById(requestId);
            
            // Validate moderator can process this request
            if (!domainApprovalRequestService.validateDecision(requestId, params.getModeratorId())) {
                throw new ApplicationModeratorNotAuthorizedException(params.getModeratorId(), "reject quote");
            }
            
            // Process rejection decision
            DomainApprovalDecisionEntity decision = domainApprovalDecisionService.rejectQuote(
                    requestId, params.getModeratorId(), params.getReason(), params.getComments());
            
            // Record audit trail
            domainAuditTrailService.recordAction(
                    requestId, 
                    "REJECTED", 
                    params.getModeratorId(), 
                    "PENDING", 
                    "REJECTED");
            
            // Publish domain event
            SharedQuoteRejectedEventDTO event = SharedQuoteRejectedEventDTO.builder()
                    .eventType("QuoteRejectedEvent")
                    .quoteId(request.getQuoteId())
                    .moderatorId(params.getModeratorId())
                    .timestamp(LocalDateTime.now().toString())
                    .reason(params.getReason())
                    .build();
            
            eventPublisher.publishQuoteRejected(event);
            
            // Send rejection notification
            createApprovalNotification("QUOTE_REJECTED", request.getSubmittedById(), 
                    "Your quote was rejected. Reason: " + params.getReason());
            
            logger.info("Quote rejected successfully for request {}", requestId);
            
            return decision.toDTO();
            
        } catch (ApplicationApprovalNotFoundException | ApplicationModeratorNotAuthorizedException e) {
            logger.error("Authorization or validation error while rejecting quote for request {}", requestId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to reject quote for request {}", requestId, e);
            throw new ApplicationInvalidStatusTransitionException("Failed to reject quote: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SharedApprovalRequestDTO assignModerator(String requestId, SharedModeratorAssignmentRequestParams params) {
        logger.info("Assigning moderator {} to request {}", params.getModeratorId(), requestId);
        
        try {
            DomainApprovalRequestEntity request = domainApprovalRequestService.assignModerator(
                    requestId, params.getModeratorId());
            
            // Record audit trail
            domainAuditTrailService.recordAction(
                    requestId, 
                    "ASSIGNED", 
                    params.getModeratorId(), 
                    "PENDING", 
                    "IN_REVIEW");
            
            // Send assignment notification
            createApprovalNotification("ASSIGNMENT", params.getModeratorId(), 
                    "You have been assigned approval request: " + requestId);
            
            logger.info("Moderator {} successfully assigned to request {}", params.getModeratorId(), requestId);
            
            return request.toDTO();
            
        } catch (Exception e) {
            logger.error("Failed to assign moderator {} to request {}", params.getModeratorId(), requestId, e);
            throw new ApplicationInvalidStatusTransitionException("Failed to assign moderator: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SharedApprovalRequestDTO processAssignment(SharedModerationAssignmentDTO assignment) {
        logger.info("Processing assignment for request {} to moderator {}", 
                assignment.getRequestId(), assignment.getModeratorId());
        
        try {
            DomainApprovalRequestEntity request = domainApprovalRequestService.assignModerator(
                    assignment.getRequestId(), assignment.getModeratorId());
            
            // Record audit trail
            domainAuditTrailService.recordAction(
                    assignment.getRequestId(), 
                    "ASSIGNED", 
                    assignment.getModeratorId(), 
                    "PENDING", 
                    "IN_REVIEW");
            
            // Send assignment notification
            createApprovalNotification("ASSIGNMENT", assignment.getModeratorId(), 
                    "You have been assigned approval request: " + assignment.getRequestId());
            
            logger.info("Assignment processed successfully for request {}", assignment.getRequestId());
            
            return request.toDTO();
            
        } catch (Exception e) {
            logger.error("Failed to process assignment for request {}", assignment.getRequestId(), e);
            throw new ApplicationInvalidStatusTransitionException("Failed to process assignment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SharedApprovalRequestDTO> retrievePendingRequests(SharedPendingRequestsQueryParams queryParams) {
        logger.debug("Retrieving pending requests with params: {}", queryParams);
        
        try {
            Map<String, Object> filter = new HashMap<>();
            if (queryParams.getModeratorId() != null) {
                filter.put("moderatorId", queryParams.getModeratorId());
            }
            if (queryParams.getPriority() != null) {
                filter.put("priority", queryParams.getPriority());
            }
            if (queryParams.getQueue() != null) {
                filter.put("queue", queryParams.getQueue());
            }
            
            List<DomainApprovalRequestEntity> requests = requestRepository.findPendingRequests(filter);
            
            return requests.stream()
                    .map(DomainApprovalRequestEntity::toDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve pending requests", e);
            throw new ApplicationApprovalNotFoundException("Failed to retrieve pending requests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SharedApprovalAuditLogDTO> retrieveAuditTrail(String requestId) {
        logger.debug("Retrieving audit trail for request {}", requestId);
        
        try {
            List<DomainApprovalAuditLogEntity> auditLogs = domainAuditTrailService.retrieveAuditTrail(requestId);
            
            return auditLogs.stream()
                    .map(DomainApprovalAuditLogEntity::toDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve audit trail for request {}", requestId, e);
            throw new ApplicationApprovalNotFoundException("Failed to retrieve audit trail: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SharedApprovalQueueDTO> retrieveQueues(SharedQueuesQueryParams queryParams) {
        logger.debug("Retrieving queues with params: {}", queryParams);
        
        try {
            // For now, return all active queues. In future, implement filtering based on queryParams
            List<DomainApprovalQueueEntity> queues = queueRepository.findActiveQueues();
            
            return queues.stream()
                    .map(DomainApprovalQueueEntity::toDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve queues", e);
            throw new ApplicationApprovalNotFoundException("Failed to retrieve queues: " + e.getMessage(), e);
        }
    }

    /**
     * Creates and sends approval workflow notifications.
     *
     * @param type the notification type
     * @param recipientId the recipient user ID
     * @param message the notification message
     */
    private void createApprovalNotification(String type, String recipientId, String message) {
        try {
            SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                    .type(type)
                    .recipientId(recipientId)
                    .message(message)
                    .build();
            
            notificationSender.sendNotification(notification);
            
        } catch (Exception e) {
            logger.error("Failed to send notification of type {} to recipient {}", type, recipientId, e);
            // Don't throw exception here as notification failure shouldn't break the main flow
        }
    }
}