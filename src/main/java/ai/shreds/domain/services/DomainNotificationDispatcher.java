package ai.shreds.domain.services;

import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain service for handling notification triggering for approval workflow events.
 * This service creates notification objects that are sent to stakeholders via message queues.
 */
@Service
@Slf4j
public class DomainNotificationDispatcher {
    
    private static final DateTimeFormatter NOTIFICATION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Notification types
    private static final String QUOTE_APPROVED = "QUOTE_APPROVED";
    private static final String QUOTE_REJECTED = "QUOTE_REJECTED";
    private static final String DEADLINE_ALERT = "DEADLINE_ALERT";
    private static final String ASSIGNMENT_NOTIFICATION = "ASSIGNMENT_NOTIFICATION";
    private static final String REVISION_REQUIRED = "REVISION_REQUIRED";
    private static final String ESCALATION_ALERT = "ESCALATION_ALERT";
    
    /**
     * Sends notification when a quote is approved.
     * 
     * @param quoteId the ID of the approved quote
     * @param moderatorId the ID of the moderator who approved the quote
     * @param recipientId the ID of the recipient (usually the submitter)
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendApprovalNotification(
            String quoteId, 
            String moderatorId, 
            String recipientId) {
        
        validateNotificationParameters(quoteId, moderatorId, recipientId);
        
        String message = String.format(
                "Great news! Your quote (ID: %s) has been approved by moderator %s at %s. "
                + "It will be published shortly and become visible to users.",
                quoteId,
                moderatorId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(QUOTE_APPROVED)
                .recipientId(recipientId)
                .message(message)
                .build();
        
        log.info("Created approval notification for quote {} to recipient {}", quoteId, recipientId);
        
        return notification;
    }
    
    /**
     * Sends notification when a quote is rejected.
     * 
     * @param quoteId the ID of the rejected quote
     * @param moderatorId the ID of the moderator who rejected the quote
     * @param recipientId the ID of the recipient (usually the submitter)
     * @param reason the reason for rejection
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendRejectionNotification(
            String quoteId, 
            String moderatorId, 
            String recipientId, 
            String reason) {
        
        validateNotificationParameters(quoteId, moderatorId, recipientId);
        validateReason(reason);
        
        String message = String.format(
                "Your quote (ID: %s) has been rejected by moderator %s at %s. "
                + "Reason: %s. You may revise and resubmit your quote if you believe this was an error.",
                quoteId,
                moderatorId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT),
                reason
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(QUOTE_REJECTED)
                .recipientId(recipientId)
                .message(message)
                .build();
        
        log.info("Created rejection notification for quote {} to recipient {} with reason: {}", 
                quoteId, recipientId, reason);
        
        return notification;
    }
    
    /**
     * Sends notification when a quote requires revision.
     * 
     * @param quoteId the ID of the quote requiring revision
     * @param moderatorId the ID of the moderator who requested revision
     * @param recipientId the ID of the recipient (usually the submitter)
     * @param reason the reason for requiring revision
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendRevisionRequiredNotification(
            String quoteId, 
            String moderatorId, 
            String recipientId, 
            String reason) {
        
        validateNotificationParameters(quoteId, moderatorId, recipientId);
        validateReason(reason);
        
        String message = String.format(
                "Your quote (ID: %s) requires revision as noted by moderator %s at %s. "
                + "Please review the feedback and make necessary changes: %s. "
                + "Once revised, your quote will be reconsidered for approval.",
                quoteId,
                moderatorId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT),
                reason
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(REVISION_REQUIRED)
                .recipientId(recipientId)
                .message(message)
                .build();
        
        log.info("Created revision required notification for quote {} to recipient {} with reason: {}", 
                quoteId, recipientId, reason);
        
        return notification;
    }
    
    /**
     * Sends alert when approval deadline is approaching.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the assigned moderator
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendDeadlineAlert(String requestId, String moderatorId) {
        validateRequestId(requestId);
        validateModeratorId(moderatorId);
        
        String message = String.format(
                "URGENT: The approval deadline for request %s is approaching. "
                + "Please review and make a decision as soon as possible. "
                + "Alert generated at %s.",
                requestId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(DEADLINE_ALERT)
                .recipientId(moderatorId)
                .message(message)
                .build();
        
        log.warn("Created deadline alert for request {} to moderator {}", requestId, moderatorId);
        
        return notification;
    }
    
    /**
     * Sends notification when an approval request is assigned to a moderator.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the assigned moderator
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendAssignmentNotification(String requestId, String moderatorId) {
        validateRequestId(requestId);
        validateModeratorId(moderatorId);
        
        String message = String.format(
                "A new approval request (ID: %s) has been assigned to you for review. "
                + "Please log in to the moderation system to review the content. "
                + "Assignment made at %s.",
                requestId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(ASSIGNMENT_NOTIFICATION)
                .recipientId(moderatorId)
                .message(message)
                .build();
        
        log.info("Created assignment notification for request {} to moderator {}", requestId, moderatorId);
        
        return notification;
    }
    
    /**
     * Sends escalation alert when a deadline has been missed.
     * 
     * @param requestId the ID of the approval request
     * @param originalModeratorId the ID of the original moderator
     * @param escalationRecipientId the ID of the escalation recipient (supervisor)
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendEscalationAlert(
            String requestId, 
            String originalModeratorId, 
            String escalationRecipientId) {
        
        validateRequestId(requestId);
        validateModeratorId(originalModeratorId);
        validateRecipientId(escalationRecipientId);
        
        String message = String.format(
                "ESCALATION ALERT: Approval request %s assigned to moderator %s has exceeded its deadline. "
                + "Please review the request and take appropriate action. "
                + "Escalation triggered at %s.",
                requestId,
                originalModeratorId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(ESCALATION_ALERT)
                .recipientId(escalationRecipientId)
                .message(message)
                .build();
        
        log.warn("Created escalation alert for request {} from moderator {} to supervisor {}", 
                requestId, originalModeratorId, escalationRecipientId);
        
        return notification;
    }
    
    /**
     * Sends notification when a high-priority request is submitted.
     * 
     * @param requestId the ID of the approval request
     * @param submitterId the ID of the submitter
     * @param moderatorId the ID of the assigned moderator
     * @param priority the priority level of the request
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendHighPriorityNotification(
            String requestId, 
            String submitterId, 
            String moderatorId, 
            String priority) {
        
        validateRequestId(requestId);
        validateModeratorId(moderatorId);
        validateRecipientId(submitterId);
        
        if (priority == null || priority.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority cannot be null or empty");
        }
        
        String message = String.format(
                "A %s priority approval request (ID: %s) has been submitted and assigned to you. "
                + "This request requires expedited processing. "
                + "Please prioritize this review. Notification sent at %s.",
                priority,
                requestId,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type("HIGH_PRIORITY_ASSIGNMENT")
                .recipientId(moderatorId)
                .message(message)
                .build();
        
        log.info("Created high-priority notification for request {} (priority: {}) to moderator {}", 
                requestId, priority, moderatorId);
        
        return notification;
    }
    
    /**
     * Sends notification when a business rule violation is detected.
     * 
     * @param requestId the ID of the approval request
     * @param moderatorId the ID of the assigned moderator
     * @param ruleName the name of the violated rule
     * @param severity the severity of the violation
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO sendRuleViolationNotification(
            String requestId, 
            String moderatorId, 
            String ruleName, 
            String severity) {
        
        validateRequestId(requestId);
        validateModeratorId(moderatorId);
        
        if (ruleName == null || ruleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty");
        }
        if (severity == null || severity.trim().isEmpty()) {
            throw new IllegalArgumentException("Severity cannot be null or empty");
        }
        
        String message = String.format(
                "RULE VIOLATION DETECTED: Request %s has triggered a %s severity violation "
                + "for rule '%s'. Please review the content carefully before making a decision. "
                + "Violation detected at %s.",
                requestId,
                severity,
                ruleName,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type("RULE_VIOLATION")
                .recipientId(moderatorId)
                .message(message)
                .build();
        
        log.warn("Created rule violation notification for request {} (rule: {}, severity: {}) to moderator {}", 
                requestId, ruleName, severity, moderatorId);
        
        return notification;
    }
    
    /**
     * Creates a custom notification with specified type and message.
     * 
     * @param type the notification type
     * @param recipientId the ID of the recipient
     * @param message the notification message
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SharedApprovalNotificationDTO createCustomNotification(
            String type, 
            String recipientId, 
            String message) {
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type cannot be null or empty");
        }
        validateRecipientId(recipientId);
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type(type)
                .recipientId(recipientId)
                .message(message)
                .build();
        
        log.info("Created custom notification of type {} to recipient {}", type, recipientId);
        
        return notification;
    }
    
    /**
     * Creates a batch completion notification when multiple requests are processed.
     * 
     * @param moderatorId the ID of the moderator
     * @param processedCount the number of requests processed
     * @param approvedCount the number of requests approved
     * @param rejectedCount the number of requests rejected
     * @return the notification DTO to be sent
     * @throws IllegalArgumentException if parameters are invalid
     */
    public SharedApprovalNotificationDTO sendBatchCompletionNotification(
            String moderatorId, 
            int processedCount, 
            int approvedCount, 
            int rejectedCount) {
        
        validateModeratorId(moderatorId);
        
        if (processedCount < 0 || approvedCount < 0 || rejectedCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }
        
        if (processedCount != (approvedCount + rejectedCount)) {
            throw new IllegalArgumentException("Processed count must equal approved + rejected counts");
        }
        
        String message = String.format(
                "Batch processing completed! You have processed %d requests: "
                + "%d approved, %d rejected. "
                + "Great work! Batch completed at %s.",
                processedCount,
                approvedCount,
                rejectedCount,
                LocalDateTime.now().format(NOTIFICATION_DATE_FORMAT)
        );
        
        SharedApprovalNotificationDTO notification = SharedApprovalNotificationDTO.builder()
                .type("BATCH_COMPLETION")
                .recipientId(moderatorId)
                .message(message)
                .build();
        
        log.info("Created batch completion notification for moderator {} (processed: {}, approved: {}, rejected: {})", 
                moderatorId, processedCount, approvedCount, rejectedCount);
        
        return notification;
    }
    
    // Validation methods
    private void validateNotificationParameters(String quoteId, String moderatorId, String recipientId) {
        if (quoteId == null || quoteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Quote ID cannot be null or empty");
        }
        validateModeratorId(moderatorId);
        validateRecipientId(recipientId);
    }
    
    private void validateRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
    }
    
    private void validateModeratorId(String moderatorId) {
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
    }
    
    private void validateRecipientId(String recipientId) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
    }
    
    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
    }
}