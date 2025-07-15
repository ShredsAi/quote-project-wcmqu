package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approval_audit_logs")
public class InfrastructureApprovalAuditLogJpaEntity {

    @Id
    @Column(name = "audit_id", nullable = false, updatable = false)
    private UUID auditId;

    @Column(name = "approval_request_id", nullable = false)
    private UUID approvalRequestId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by_id", nullable = false)
    private UUID performedById;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    public DomainApprovalAuditLogEntity toDomainEntity() {
        DomainApprovalAuditLogEntity domain = new DomainApprovalAuditLogEntity();
        domain.setAuditId(this.auditId != null ? this.auditId.toString() : null);
        domain.setApprovalRequestId(this.approvalRequestId != null ? this.approvalRequestId.toString() : null);
        domain.setAction(this.action);
        domain.setPerformedById(this.performedById != null ? this.performedById.toString() : null);
        domain.setOldValue(this.oldValue);
        domain.setNewValue(this.newValue);
        domain.setTimestamp(this.timestamp);
        domain.setIpAddress(this.ipAddress);
        domain.setUserAgent(this.userAgent);
        return domain;
    }

    public static InfrastructureApprovalAuditLogJpaEntity fromDomainEntity(DomainApprovalAuditLogEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureApprovalAuditLogJpaEntity.builder()
                .auditId(domain.getAuditId() != null ? UUID.fromString(domain.getAuditId()) : UUID.randomUUID())
                .approvalRequestId(domain.getApprovalRequestId() != null ? UUID.fromString(domain.getApprovalRequestId()) : null)
                .action(domain.getAction())
                .performedById(domain.getPerformedById() != null ? UUID.fromString(domain.getPerformedById()) : null)
                .oldValue(domain.getOldValue())
                .newValue(domain.getNewValue())
                .timestamp(domain.getTimestamp())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .build();
    }
}