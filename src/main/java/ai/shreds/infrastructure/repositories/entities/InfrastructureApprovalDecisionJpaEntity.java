package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.value_objects.DomainDecisionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approval_decisions")
public class InfrastructureApprovalDecisionJpaEntity {

    @Id
    @Column(name = "decision_id", nullable = false, updatable = false)
    private UUID decisionId;

    @Column(name = "approval_request_id", nullable = false)
    private UUID approvalRequestId;

    @Column(name = "moderator_id", nullable = false)
    private UUID moderatorId;

    @Column(name = "decision", nullable = false)
    private String decision;

    @Column(name = "reason")
    private String reason;

    @Column(name = "comments")
    private String comments;

    @Column(name = "decision_timestamp", nullable = false)
    private LocalDateTime decisionTimestamp;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    public DomainApprovalDecisionEntity toDomainEntity() {
        DomainApprovalDecisionEntity domain = new DomainApprovalDecisionEntity();
        domain.setDecisionId(this.decisionId != null ? this.decisionId.toString() : null);
        domain.setApprovalRequestId(this.approvalRequestId != null ? this.approvalRequestId.toString() : null);
        domain.setModeratorId(this.moderatorId != null ? this.moderatorId.toString() : null);
        domain.setDecision(DomainDecisionType.valueOf(this.decision));
        domain.setReason(this.reason);
        domain.setComments(this.comments);
        domain.setDecisionTimestamp(this.decisionTimestamp);
        domain.setProcessingTimeMs(this.processingTimeMs);
        return domain;
    }

    public static InfrastructureApprovalDecisionJpaEntity fromDomainEntity(DomainApprovalDecisionEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureApprovalDecisionJpaEntity.builder()
            .decisionId(domain.getDecisionId() != null ? UUID.fromString(domain.getDecisionId()) : UUID.randomUUID())
            .approvalRequestId(domain.getApprovalRequestId() != null ? UUID.fromString(domain.getApprovalRequestId()) : null)
            .moderatorId(domain.getModeratorId() != null ? UUID.fromString(domain.getModeratorId()) : null)
            .decision(domain.getDecision().name())
            .reason(domain.getReason())
            .comments(domain.getComments())
            .decisionTimestamp(domain.getDecisionTimestamp())
            .processingTimeMs(domain.getProcessingTimeMs())
            .build();
    }
}