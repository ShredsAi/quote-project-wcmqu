package ai.shreds.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing an audit log entry for approval workflow activities.
 * This entity maintains a comprehensive audit trail of all actions performed on approval requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainApprovalAuditLogEntity {
    private String auditId;
    private String approvalRequestId;
    private String action;
    private String performedById;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Validates the audit log data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the audit log data is invalid
     */
    public void validate() {
        if (approvalRequestId == null || approvalRequestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Approval request ID cannot be null or empty");
        }
        
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        
        if (performedById == null || performedById.trim().isEmpty()) {
            throw new IllegalArgumentException("Performed by ID cannot be null or empty");
        }
        
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }

    /**
     * Initializes the audit log entry with current timestamp.
     */
    public void initializeAuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates an audit log entry for a status change.
     * 
     * @param approvalRequestId the ID of the approval request
     * @param performedById the ID of the user who performed the action
     * @param oldStatus the previous status
     * @param newStatus the new status
     * @param ipAddress the IP address of the requester
     * @param userAgent the user agent of the requester
     * @return a new audit log entity
     */
    public static DomainApprovalAuditLogEntity createStatusChangeEntry(
            String approvalRequestId, 
            String performedById, 
            String oldStatus, 
            String newStatus,
            String ipAddress,
            String userAgent) {
        return DomainApprovalAuditLogEntity.builder()
                .approvalRequestId(approvalRequestId)
                .action("STATUS_CHANGE")
                .performedById(performedById)
                .oldValue(oldStatus)
                .newValue(newStatus)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Creates an audit log entry for moderator assignment.
     * 
     * @param approvalRequestId the ID of the approval request
     * @param performedById the ID of the user who performed the action
     * @param oldModeratorId the previous moderator ID (can be null)
     * @param newModeratorId the new moderator ID
     * @param ipAddress the IP address of the requester
     * @param userAgent the user agent of the requester
     * @return a new audit log entity
     */
    public static DomainApprovalAuditLogEntity createModeratorAssignmentEntry(
            String approvalRequestId, 
            String performedById, 
            String oldModeratorId, 
            String newModeratorId,
            String ipAddress,
            String userAgent) {
        return DomainApprovalAuditLogEntity.builder()
                .approvalRequestId(approvalRequestId)
                .action("MODERATOR_ASSIGNMENT")
                .performedById(performedById)
                .oldValue(oldModeratorId)
                .newValue(newModeratorId)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Creates an audit log entry for decision made.
     * 
     * @param approvalRequestId the ID of the approval request
     * @param performedById the ID of the moderator who made the decision
     * @param decision the decision made
     * @param reason the reason for the decision
     * @param ipAddress the IP address of the requester
     * @param userAgent the user agent of the requester
     * @return a new audit log entity
     */
    public static DomainApprovalAuditLogEntity createDecisionEntry(
            String approvalRequestId, 
            String performedById, 
            String decision,
            String reason,
            String ipAddress,
            String userAgent) {
        return DomainApprovalAuditLogEntity.builder()
                .approvalRequestId(approvalRequestId)
                .action("DECISION_MADE")
                .performedById(performedById)
                .oldValue(null)
                .newValue(decision + (reason != null ? " - " + reason : ""))
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Gets the formatted timestamp as a string.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(DATE_FORMATTER) : null;
    }
}