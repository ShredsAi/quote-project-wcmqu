package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approval_queues")
public class InfrastructureApprovalQueueJpaEntity {

    @Id
    @Column(name = "queue_id", nullable = false, updatable = false)
    private String queueId;

    @Column(name = "queue_name", nullable = false, unique = true)
    private String queueName;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_size", nullable = false)
    private Integer currentSize;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    public DomainApprovalQueueEntity toDomainEntity() {
        DomainApprovalQueueEntity domain = new DomainApprovalQueueEntity();
        domain.setQueueId(this.queueId);
        domain.setQueueName(this.queueName);
        domain.setMaxCapacity(this.maxCapacity);
        domain.setCurrentSize(this.currentSize);
        domain.setIsActive(this.isActive);
        domain.setCreatedAt(this.createdAt);
        domain.setLastProcessedAt(this.lastProcessedAt);
        return domain;
    }

    public static InfrastructureApprovalQueueJpaEntity fromDomainEntity(DomainApprovalQueueEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureApprovalQueueJpaEntity.builder()
                .queueId(domain.getQueueId())
                .queueName(domain.getQueueName())
                .maxCapacity(domain.getMaxCapacity())
                .currentSize(domain.getCurrentSize())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .lastProcessedAt(domain.getLastProcessedAt())
                .build();
    }
}