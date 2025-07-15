package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
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
    private UUID approvalRequestId;

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "submitted_by_id", nullable = false)
    private UUID submittedById;

    @Column(name = "priority", nullable = false)
    private String priority;

    @Column(name = "assigned_moderator_id")
    private UUID assignedModeratorId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "queue_id")
    private UUID queueId;

    public DomainApprovalRequestEntity toDomainEntity() {
        DomainApprovalRequestEntity domain = new DomainApprovalRequestEntity();
        domain.setApprovalRequestId(this.approvalRequestId != null ? this.approvalRequestId.toString() : null);
        domain.setQuoteId(this.quoteId != null ? this.quoteId.toString() : null);
        domain.setSubmittedById(this.submittedById != null ? this.submittedById.toString() : null);
        domain.setPriority(DomainPriority.valueOf(this.priority));
        domain.setAssignedModeratorId(this.assignedModeratorId != null ? this.assignedModeratorId.toString() : null);
        domain.setStatus(DomainApprovalStatus.valueOf(this.status));
        domain.setSubmittedAt(this.submittedAt);
        domain.setAssignedAt(this.assignedAt);
        domain.setDeadline(this.deadline);
        domain.setQueueId(this.queueId != null ? this.queueId.toString() : null);
        return domain;
    }

    public static InfrastructureApprovalRequestJpaEntity fromDomainEntity(DomainApprovalRequestEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureApprovalRequestJpaEntity.builder()
            .approvalRequestId(domain.getApprovalRequestId() != null ? UUID.fromString(domain.getApprovalRequestId()) : UUID.randomUUID())
            .quoteId(domain.getQuoteId() != null ? UUID.fromString(domain.getQuoteId()) : null)
            .submittedById(domain.getSubmittedById() != null ? UUID.fromString(domain.getSubmittedById()) : null)
            .priority(domain.getPriority().name())
            .assignedModeratorId(domain.getAssignedModeratorId() != null ? UUID.fromString(domain.getAssignedModeratorId()) : null)
            .status(domain.getStatus().name())
            .submittedAt(domain.getSubmittedAt())
            .assignedAt(domain.getAssignedAt())
            .deadline(domain.getDeadline())
            .queueId(domain.getQueueId() != null ? UUID.fromString(domain.getQueueId()) : null)
            .build();
    }
}