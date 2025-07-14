package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainInvalidStatusTransitionException;
import ai.shreds.domain.exceptions.DomainModeratorNotAuthorizedException;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing an approval request for a quote.
 * This entity encapsulates all the business rules related to approval requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainApprovalRequestEntity {
    private String approvalRequestId;
    private String quoteId;
    private String submittedById;
    private DomainPriority priority;
    private String assignedModeratorId;
    private DomainApprovalStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deadline;
    private String queueId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Assigns a moderator to this approval request.
     * Updates the status to IN_REVIEW if it's currently PENDING.
     * 
     * @param moderatorId the ID of the moderator to assign
     * @throws DomainInvalidStatusTransitionException if the request is not in PENDING status
     */
    public void assignModerator(String moderatorId) {
        if (status != DomainApprovalStatus.PENDING) {
            throw new DomainInvalidStatusTransitionException(
                    status.toString(), 
                    DomainApprovalStatus.IN_REVIEW.toString());
        }
        
        this.assignedModeratorId = moderatorId;
        this.assignedAt = LocalDateTime.now();
        this.status = DomainApprovalStatus.IN_REVIEW;
    }

    /**
     * Updates the status of this approval request.
     * 
     * @param newStatus the new status to set
     * @throws DomainInvalidStatusTransitionException if the status transition is not allowed
     */
    public void updateStatus(DomainApprovalStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new DomainInvalidStatusTransitionException(status.toString(), newStatus.toString());
        }
        
        this.status = newStatus;
    }

    /**
     * Checks if this approval request can be processed by the given moderator.
     * 
     * @param moderatorId the ID of the moderator to check
     * @return true if the moderator can process this request
     */
    public boolean canBeProcessedBy(String moderatorId) {
        // Only the assigned moderator can process the request
        // If no moderator is assigned yet, nobody can process it
        return this.assignedModeratorId != null && this.assignedModeratorId.equals(moderatorId);
    }

    /**
     * Validates that the given moderator is authorized to make decisions on this request.
     * 
     * @param moderatorId the ID of the moderator to validate
     * @throws DomainModeratorNotAuthorizedException if the moderator is not authorized
     */
    public void validateModeratorAuthorization(String moderatorId) {
        if (!canBeProcessedBy(moderatorId)) {
            throw new DomainModeratorNotAuthorizedException(moderatorId, this.approvalRequestId);
        }
    }

    /**
     * Checks if the deadline for this approval request has passed.
     * 
     * @return true if the request is expired (past deadline)
     */
    public boolean isExpired() {
        return this.deadline != null && LocalDateTime.now().isAfter(this.deadline);
    }

    /**
     * Calculates and sets the deadline based on the priority level.
     */
    public void calculateDeadline() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        
        int deadlineHours = this.priority.getDeadlineHours();
        this.deadline = this.submittedAt.plusHours(deadlineHours);
    }
    
    /**
     * Converts this entity to a DTO for external communication.
     * 
     * @return a DTO representing this entity
     */
    public SharedApprovalRequestDTO toDTO() {
        return SharedApprovalRequestDTO.builder()
                .approvalRequestId(approvalRequestId)
                .quoteId(quoteId)
                .submittedById(submittedById)
                .priority(priority.toString())
                .assignedModeratorId(assignedModeratorId)
                .status(status.toString())
                .submittedAt(submittedAt != null ? submittedAt.format(DATE_FORMATTER) : null)
                .assignedAt(assignedAt != null ? assignedAt.format(DATE_FORMATTER) : null)
                .deadline(deadline != null ? deadline.format(DATE_FORMATTER) : null)
                .queueId(queueId)
                .build();
    }
}