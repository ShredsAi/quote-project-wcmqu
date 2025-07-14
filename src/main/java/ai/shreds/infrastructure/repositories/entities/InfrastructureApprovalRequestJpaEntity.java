package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.domain.value_objects.DomainApprovalStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approval_requests")
public class InfrastructureApprovalRequestJpaEntity {

    @Id
    @Column(name = "approval_request_id", nullable = false, updatable = false)
    private String approvalRequestId;

    @Column(name = "quote_id", nullable = false)
    private String quoteId;

    @Column(name = "submitted_by_id", nullable = false)
    private String submittedById;

    @Column(name = "priority", nullable = false)
    private String priority;

    @Column(name = "assigned_moderator_id")
    private String assignedModeratorId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "queue_id")
    private String queueId;

    public DomainApprovalRequestEntity toDomainEntity() {
        DomainApprovalRequestEntity domain = new DomainApprovalRequestEntity();
        domain.setApprovalRequestId(this.approvalRequestId);
        domain.setQuoteId(this.quoteId);
        domain.setSubmittedById(this.submittedById);
        domain.setPriority(DomainPriority.valueOf(this.priority));
        domain.setAssignedModeratorId(this.assignedModeratorId);
        domain.setStatus(DomainApprovalStatus.valueOf(this.status));
        domain.setSubmittedAt(this.submittedAt);
        domain.setAssignedAt(this.assignedAt);
        domain.setDeadline(this.deadline);
        domain.setQueueId(this.queueId);
        return domain;
    }

    public static InfrastructureApprovalRequestJpaEntity fromDomainEntity(DomainApprovalRequestEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureApprovalRequestJpaEntity.builder()
            .approvalRequestId(domain.getApprovalRequestId())
            .quoteId(domain.getQuoteId())
            .submittedById(domain.getSubmittedById())
            .priority(domain.getPriority().name())
            .assignedModeratorId(domain.getAssignedModeratorId())
            .status(domain.getStatus().name())
            .submittedAt(domain.getSubmittedAt())
            .assignedAt(domain.getAssignedAt())
            .deadline(domain.getDeadline())
            .queueId(domain.getQueueId())
            .build();
    }
}