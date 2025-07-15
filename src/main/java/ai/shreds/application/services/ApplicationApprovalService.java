package ai.shreds.application.services;

import ai.shreds.application.events.ApplicationNotificationRequestEvent;
import ai.shreds.application.exceptions.ApplicationApprovalNotFoundException;
import ai.shreds.application.exceptions.ApplicationInvalidStatusTransitionException;
import ai.shreds.application.exceptions.ApplicationModeratorNotAuthorizedException;
import ai.shreds.application.ports.*;
import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.exceptions.DomainBusinessRuleViolationException;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.services.*;
import ai.shreds.shared.dtos.*;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;
import ai.shreds.shared.value_objects.SharedPendingRequestsQueryParams;
import ai.shreds.shared.value_objects.SharedQueuesQueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher applicationEventPublisher;

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
            ApplicationEventPublisher applicationEventPublisher) {
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
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public SharedApprovalRequestDTO submitForApproval(SharedQuoteCreatedEventDTO quoteEvent) {
        logger.info("Submitting quote {} for approval", quoteEvent.getQuoteId());
        try {
            DomainApprovalRequestEntity request = domainApprovalRequestService.createApprovalRequest(
                    quoteEvent.getQuoteId(),
                    quoteEvent.getSubmittedBy(),
                    quoteEvent.getPriority());

            Map<String, Object> ruleResults = domainBusinessRulesEngine.executeRules(request);
            logger.debug("Business rules execution results: {}", ruleResults);

            domainApprovalQueueService.addToQueue(request, request.getQueueId());

            domainAuditTrailService.recordAction(
                    request.getApprovalRequestId(),
                    "CREATED",
                    quoteEvent.getSubmittedBy(),
                    null,
                    "Approval request created for quote: " + quoteEvent.getQuoteId());

            applicationEventPublisher.publishEvent(
                new ApplicationNotificationRequestEvent(this,
                                                        "QUOTE_SUBMITTED",
                                                        "system-moderator",
                                                        "New quote submitted for approval: " + quoteEvent.getQuoteId()));

            logger.info("Quote {} successfully submitted for approval with request ID {}",
                    quoteEvent.getQuoteId(), request.getApprovalRequestId());

            return request.toDTO();
        } catch (DomainBusinessRuleViolationException e) {
            logger.error("Business rule violation for quote {}: {}", quoteEvent.getQuoteId(), e.getMessage());
            throw new ApplicationInvalidStatusTransitionException(
                    "Submission failed due to business rule violation: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while submitting quote {} for approval: {}", quoteEvent.getQuoteId(), e.getMessage());
            throw new ApplicationApprovalNotFoundException(
                    "Failed to submit quote for approval due to invalid data: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SharedApprovalDecisionDTO approveQuote(String requestId, SharedApprovalDecisionRequestParams params) {
        logger.info("Approving quote for request {} by moderator {}", requestId, params.getModeratorId());
        try {
            DomainApprovalRequestEntity request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ApplicationApprovalNotFoundException("Approval request not found: " + requestId));

            if (!request.canBeProcessedBy(params.getModeratorId())) {
                throw new ApplicationModeratorNotAuthorizedException(params.getModeratorId(), "approve quote");
            }

            DomainApprovalDecisionEntity decision = domainApprovalDecisionService.approveQuote(
                    requestId, params.getModeratorId(), params.getComments());

            domainAuditTrailService.recordAction(
                    requestId,
                    "APPROVED",
                    params.getModeratorId(),
                    request.getStatus().name(),
                    "APPROVED");

            SharedQuoteApprovedEventDTO event = new SharedQuoteApprovedEventDTO();
            event.setEventType("QuoteApprovedEvent");
            event.setQuoteId(request.getQuoteId());
            event.setModeratorId(params.getModeratorId());
            event.setTimestamp(LocalDateTime.now().toString());
            eventPublisher.publishQuoteApproved(event);

            applicationEventPublisher.publishEvent(
                new ApplicationNotificationRequestEvent(this,
                                                        "QUOTE_APPROVED",
                                                        request.getSubmittedById(),
                                                        "Your quote has been approved by moderator."));

            logger.info("Quote approved successfully for request {}", requestId);
            return decision.toDTO();
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
            DomainApprovalRequestEntity request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ApplicationApprovalNotFoundException("Approval request not found: " + requestId));

            if (!request.canBeProcessedBy(params.getModeratorId())) {
                throw new ApplicationModeratorNotAuthorizedException(params.getModeratorId(), "reject quote");
            }

            DomainApprovalDecisionEntity decision = domainApprovalDecisionService.rejectQuote(
                    requestId, params.getModeratorId(), params.getReason(), params.getComments());

            domainAuditTrailService.recordAction(
                    requestId,
                    "REJECTED",
                    params.getModeratorId(),
                    request.getStatus().name(),
                    "REJECTED");

            SharedQuoteRejectedEventDTO event = new SharedQuoteRejectedEventDTO();
            event.setEventType("QuoteRejectedEvent");
            event.setQuoteId(request.getQuoteId());
            event.setModeratorId(params.getModeratorId());
            event.setTimestamp(LocalDateTime.now().toString());
            event.setReason(params.getReason());
            eventPublisher.publishQuoteRejected(event);

            applicationEventPublisher.publishEvent(
                new ApplicationNotificationRequestEvent(this,
                                                        "QUOTE_REJECTED",
                                                        request.getSubmittedById(),
                                                        "Your quote was rejected. Reason: " + params.getReason()));

            logger.info("Quote rejected successfully for request {}", requestId);
            return decision.toDTO();
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

            applicationEventPublisher.publishEvent(
                new ApplicationNotificationRequestEvent(this,
                                                        "ASSIGNMENT",
                                                        params.getModeratorId(),
                                                        "You have been assigned approval request: " + requestId));

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
        logger.info("Processing assignment for request {} to moderator {}", assignment.getRequestId(), assignment.getModeratorId());
        try {
            DomainApprovalRequestEntity request = domainApprovalRequestService.assignModerator(
                    assignment.getRequestId(), assignment.getModeratorId());

            domainAuditTrailService.recordAction(
                    assignment.getRequestId(),
                    "ASSIGNED",
                    "system",
                    "PENDING",
                    "IN_REVIEW");

            applicationEventPublisher.publishEvent(
                new ApplicationNotificationRequestEvent(this,
                                                        "ASSIGNMENT",
                                                        assignment.getModeratorId(),
                                                        "You have been assigned approval request: " + assignment.getRequestId()));

            logger.info("Assignment processed successfully for request {} to moderator {}", assignment.getRequestId(), assignment.getModeratorId());
            return request.toDTO();
        } catch (Exception e) {
            logger.error("Failed to process assignment for request {} to moderator {}", assignment.getRequestId(), assignment.getModeratorId(), e);
            throw new ApplicationInvalidStatusTransitionException("Failed to process assignment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SharedApprovalRequestDTO> retrievePendingRequests(SharedPendingRequestsQueryParams queryParams) {
        logger.debug("Retrieving pending requests with params: {}", queryParams);
        Map<String, Object> filter = new HashMap<>();
        if (queryParams.getModeratorId() != null) filter.put("moderatorId", queryParams.getModeratorId());
        if (queryParams.getPriority() != null) filter.put("priority", queryParams.getPriority());
        if (queryParams.getQueue() != null) filter.put("queue", queryParams.getQueue());

        return requestRepository.findPendingRequests(filter).stream()
                .map(DomainApprovalRequestEntity::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SharedApprovalAuditLogDTO> retrieveAuditTrail(String requestId) {
        logger.debug("Retrieving audit trail for request {}", requestId);
        return domainAuditTrailService.retrieveAuditTrail(requestId).stream()
                .map(DomainApprovalAuditLogEntity::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SharedApprovalQueueDTO> retrieveQueues(SharedQueuesQueryParams queryParams) {
        logger.debug("Retrieving queues with params: {}", queryParams);
        return queueRepository.findActiveQueues().stream()
                .map(DomainApprovalQueueEntity::toDTO)
                .collect(Collectors.toList());
    }
}