package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing an approval request in the system.
 * Used for transferring approval request data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedApprovalRequestDTO {
    @NotBlank
    private String approvalRequestId;
    
    @NotBlank
    private String quoteId;
    
    @NotBlank
    private String submittedById;
    
    @NotBlank
    private String priority;
    
    private String assignedModeratorId;
    
    @NotBlank
    private String status;
    
    @NotNull
    private String submittedAt;
    
    private String assignedAt;
    private String deadline;
    private String queueId;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Converts this DTO to a domain entity
     * 
     * @return The domain entity representation of this DTO
     */
    public DomainApprovalRequestEntity toEntity() {
        DomainApprovalRequestEntity entity = new DomainApprovalRequestEntity();
        entity.setApprovalRequestId(this.approvalRequestId);
        entity.setQuoteId(this.quoteId);
        entity.setSubmittedById(this.submittedById);
        entity.setPriority(DomainPriority.valueOf(this.priority));
        entity.setAssignedModeratorId(this.assignedModeratorId);
        entity.setStatus(DomainApprovalStatus.valueOf(this.status));
        entity.setSubmittedAt(LocalDateTime.parse(this.submittedAt, DATE_FORMATTER));
        entity.setAssignedAt(this.assignedAt != null ? LocalDateTime.parse(this.assignedAt, DATE_FORMATTER) : null);
        entity.setDeadline(this.deadline != null ? LocalDateTime.parse(this.deadline, DATE_FORMATTER) : null);
        entity.setQueueId(this.queueId);
        return entity;
    }
    
    /**
     * Creates a DTO from a domain entity
     * 
     * @param entity The domain entity to convert
     * @return A new DTO representing the entity
     */
    public static SharedApprovalRequestDTO fromEntity(DomainApprovalRequestEntity entity) {
        return SharedApprovalRequestDTO.builder()
                .approvalRequestId(entity.getApprovalRequestId())
                .quoteId(entity.getQuoteId())
                .submittedById(entity.getSubmittedById())
                .priority(entity.getPriority().name())
                .assignedModeratorId(entity.getAssignedModeratorId())
                .status(entity.getStatus().name())
                .submittedAt(entity.getSubmittedAt().format(DATE_FORMATTER))
                .assignedAt(entity.getAssignedAt() != null ? entity.getAssignedAt().format(DATE_FORMATTER) : null)
                .deadline(entity.getDeadline() != null ? entity.getDeadline().format(DATE_FORMATTER) : null)
                .queueId(entity.getQueueId())
                .build();
    }
}