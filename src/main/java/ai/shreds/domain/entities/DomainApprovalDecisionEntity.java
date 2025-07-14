package ai.shreds.domain.entities;

import ai.shreds.domain.value_objects.DomainDecisionType;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing a decision made on an approval request.
 * This entity encapsulates all the business rules related to approval decisions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainApprovalDecisionEntity {
    private String decisionId;
    private String approvalRequestId;
    private String moderatorId;
    private DomainDecisionType decision;
    private String reason;
    private String comments;
    private LocalDateTime decisionTimestamp;
    private Long processingTimeMs;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Validates the decision data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the decision data is invalid
     */
    public void validate() {
        if (decision == null) {
            throw new IllegalArgumentException("Decision type cannot be null");
        }
        
        if (moderatorId == null || moderatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Moderator ID cannot be null or empty");
        }
        
        if (approvalRequestId == null || approvalRequestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Approval request ID cannot be null or empty");
        }
        
        // Validate that reason is provided for decisions that require it
        if (decision.requiresReason() && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException(
                String.format("Reason is required for decision type: %s", decision)
            );
        }
    }

    /**
     * Calculates the processing time based on the decision timestamp and assignment time.
     * 
     * @param assignmentTime the time when the request was assigned to the moderator
     */
    public void calculateProcessingTime(LocalDateTime assignmentTime) {
        if (assignmentTime != null && decisionTimestamp != null) {
            long timeDifferenceMs = java.time.Duration.between(assignmentTime, decisionTimestamp).toMillis();
            this.processingTimeMs = timeDifferenceMs;
        }
    }

    /**
     * Checks if this decision is final (no further processing needed).
     * 
     * @return true if the decision is final
     */
    public boolean isFinal() {
        return decision.isFinal();
    }

    /**
     * Gets the formatted decision timestamp as a string.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedDecisionTimestamp() {
        return decisionTimestamp != null ? decisionTimestamp.format(DATE_FORMATTER) : null;
    }

    /**
     * Converts this entity to a DTO for external communication.
     * 
     * @return a DTO representing this entity
     */
    public SharedApprovalDecisionDTO toDTO() {
        return SharedApprovalDecisionDTO.builder()
                .decisionId(decisionId)
                .approvalRequestId(approvalRequestId)
                .moderatorId(moderatorId)
                .decision(decision.toString())
                .reason(reason)
                .comments(comments)
                .decisionTimestamp(getFormattedDecisionTimestamp())
                .processingTimeMs(processingTimeMs)
                .build();
    }

    /**
     * Creates a new decision entity from a DTO.
     * 
     * @param dto the DTO to convert from
     * @return a new DomainApprovalDecisionEntity
     */
    public static DomainApprovalDecisionEntity fromDTO(SharedApprovalDecisionDTO dto) {
        return DomainApprovalDecisionEntity.builder()
                .decisionId(dto.getDecisionId())
                .approvalRequestId(dto.getApprovalRequestId())
                .moderatorId(dto.getModeratorId())
                .decision(DomainDecisionType.valueOf(dto.getDecision()))
                .reason(dto.getReason())
                .comments(dto.getComments())
                .decisionTimestamp(dto.getDecisionTimestamp() != null ? 
                    LocalDateTime.parse(dto.getDecisionTimestamp(), DATE_FORMATTER) : null)
                .processingTimeMs(dto.getProcessingTimeMs())
                .build();
    }

    /**
     * Initializes the decision with current timestamp.
     */
    public void initializeDecision() {
        this.decisionTimestamp = LocalDateTime.now();
    }
}