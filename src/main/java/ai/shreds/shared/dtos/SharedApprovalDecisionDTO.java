package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.value_objects.DomainDecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing an approval decision in the system.
 * Used for transferring approval decision data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedApprovalDecisionDTO {
    @NotBlank
    private String decisionId;
    
    @NotBlank
    private String approvalRequestId;
    
    @NotBlank
    private String moderatorId;
    
    @NotBlank
    private String decision;
    
    private String reason;
    private String comments;
    
    @NotNull
    private String decisionTimestamp;
    
    private Long processingTimeMs;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Creates a DTO from a domain entity
     * 
     * @param entity The domain entity to convert
     * @return A new DTO representing the entity
     */
    public static SharedApprovalDecisionDTO fromEntity(DomainApprovalDecisionEntity entity) {
        return SharedApprovalDecisionDTO.builder()
                .decisionId(entity.getDecisionId())
                .approvalRequestId(entity.getApprovalRequestId())
                .moderatorId(entity.getModeratorId())
                .decision(entity.getDecision().name())
                .reason(entity.getReason())
                .comments(entity.getComments())
                .decisionTimestamp(entity.getDecisionTimestamp().format(DATE_FORMATTER))
                .processingTimeMs(entity.getProcessingTimeMs())
                .build();
    }
    
    /**
     * Converts this DTO to a domain entity
     * 
     * @return The domain entity representation of this DTO
     */
    public DomainApprovalDecisionEntity toEntity() {
        DomainApprovalDecisionEntity entity = new DomainApprovalDecisionEntity();
        entity.setDecisionId(this.decisionId);
        entity.setApprovalRequestId(this.approvalRequestId);
        entity.setModeratorId(this.moderatorId);
        entity.setDecision(DomainDecisionType.valueOf(this.decision));
        entity.setReason(this.reason);
        entity.setComments(this.comments);
        entity.setDecisionTimestamp(LocalDateTime.parse(this.decisionTimestamp, DATE_FORMATTER));
        entity.setProcessingTimeMs(this.processingTimeMs);
        return entity;
    }
}