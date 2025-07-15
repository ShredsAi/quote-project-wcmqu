package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import ai.shreds.domain.entities.DomainModerationRuleEntity;
import ai.shreds.domain.value_objects.DomainRuleType;
import ai.shreds.domain.value_objects.DomainRuleSeverity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "moderation_rules")
public class InfrastructureModerationRuleJpaEntity {

    @Id
    @Column(name = "rule_id", nullable = false, updatable = false)
    private UUID ruleId;

    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;

    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    @Column(name = "description")
    private String description;

    @Column(name = "rule_expression", nullable = false)
    private String ruleExpression;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DomainModerationRuleEntity toDomainEntity() {
        DomainModerationRuleEntity domain = new DomainModerationRuleEntity();
        domain.setRuleId(this.ruleId != null ? this.ruleId.toString() : null);
        domain.setRuleName(this.ruleName);
        domain.setRuleType(DomainRuleType.valueOf(this.ruleType));
        domain.setDescription(this.description);
        domain.setRuleExpression(this.ruleExpression);
        domain.setSeverity(DomainRuleSeverity.valueOf(this.severity));
        domain.setIsActive(this.isActive);
        domain.setCreatedAt(this.createdAt);
        return domain;
    }

    public static InfrastructureModerationRuleJpaEntity fromDomainEntity(DomainModerationRuleEntity domain) {
        if (domain == null) {
            return null;
        }
        return InfrastructureModerationRuleJpaEntity.builder()
                .ruleId(domain.getRuleId() != null ? UUID.fromString(domain.getRuleId()) : UUID.randomUUID())
                .ruleName(domain.getRuleName())
                .ruleType(domain.getRuleType().name())
                .description(domain.getDescription())
                .ruleExpression(domain.getRuleExpression())
                .severity(domain.getSeverity().name())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}