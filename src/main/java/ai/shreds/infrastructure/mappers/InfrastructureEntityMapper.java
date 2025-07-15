package ai.shreds.infrastructure.mappers;

import ai.shreds.domain.entities.*;
import ai.shreds.infrastructure.repositories.entities.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InfrastructureEntityMapper {

    // ApprovalRequest mappings
    public DomainApprovalRequestEntity toApprovalRequestDomain(InfrastructureApprovalRequestJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return jpaEntity.toDomainEntity();
    }

    public InfrastructureApprovalRequestJpaEntity toApprovalRequestJpa(DomainApprovalRequestEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        return InfrastructureApprovalRequestJpaEntity.fromDomainEntity(domainEntity);
    }

    public List<DomainApprovalRequestEntity> toApprovalRequestDomainList(List<InfrastructureApprovalRequestJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        return jpaEntities.stream()
            .map(this::toApprovalRequestDomain)
            .collect(Collectors.toList());
    }

    // ApprovalDecision mappings
    public DomainApprovalDecisionEntity toApprovalDecisionDomain(InfrastructureApprovalDecisionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return jpaEntity.toDomainEntity();
    }

    public InfrastructureApprovalDecisionJpaEntity toApprovalDecisionJpa(DomainApprovalDecisionEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        return InfrastructureApprovalDecisionJpaEntity.fromDomainEntity(domainEntity);
    }

    public List<DomainApprovalDecisionEntity> toApprovalDecisionDomainList(List<InfrastructureApprovalDecisionJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        return jpaEntities.stream()
            .map(this::toApprovalDecisionDomain)
            .collect(Collectors.toList());
    }

    // AuditLog mappings
    public DomainApprovalAuditLogEntity toAuditLogDomain(InfrastructureApprovalAuditLogJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return jpaEntity.toDomainEntity();
    }

    public InfrastructureApprovalAuditLogJpaEntity toAuditLogJpa(DomainApprovalAuditLogEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        return InfrastructureApprovalAuditLogJpaEntity.fromDomainEntity(domainEntity);
    }

    public List<DomainApprovalAuditLogEntity> toAuditLogDomainList(List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        return jpaEntities.stream()
            .map(this::toAuditLogDomain)
            .collect(Collectors.toList());
    }

    // ApprovalQueue mappings
    public DomainApprovalQueueEntity toApprovalQueueDomain(InfrastructureApprovalQueueJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return jpaEntity.toDomainEntity();
    }

    public InfrastructureApprovalQueueJpaEntity toApprovalQueueJpa(DomainApprovalQueueEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        return InfrastructureApprovalQueueJpaEntity.fromDomainEntity(domainEntity);
    }

    public List<DomainApprovalQueueEntity> toApprovalQueueDomainList(List<InfrastructureApprovalQueueJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        return jpaEntities.stream()
            .map(this::toApprovalQueueDomain)
            .collect(Collectors.toList());
    }

    // ModerationRule mappings
    public DomainModerationRuleEntity toModerationRuleDomain(InfrastructureModerationRuleJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return jpaEntity.toDomainEntity();
    }

    public InfrastructureModerationRuleJpaEntity toModerationRuleJpa(DomainModerationRuleEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        return InfrastructureModerationRuleJpaEntity.fromDomainEntity(domainEntity);
    }

    public List<DomainModerationRuleEntity> toModerationRuleDomainList(List<InfrastructureModerationRuleJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        return jpaEntities.stream()
            .map(this::toModerationRuleDomain)
            .collect(Collectors.toList());
    }
}