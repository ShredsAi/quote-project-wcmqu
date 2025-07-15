package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing the execution of moderation rules on approval requests.
 * This entity serves as a join table that tracks which rules have been executed
 * on which approval requests, along with their results and execution details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approval_request_rule_executions", indexes = {
    @Index(name = "idx_rule_execution_timestamp", columnList = "execution_timestamp"),
    @Index(name = "idx_rule_execution_result", columnList = "result"),
    @Index(name = "idx_rule_execution_approval_request", columnList = "approval_request_id"),
    @Index(name = "idx_rule_execution_rule", columnList = "rule_id")
})
@IdClass(InfrastructureApprovalRequestRuleExecutionId.class)
public class InfrastructureApprovalRequestRuleExecutionJpaEntity {

    /**
     * The ID of the approval request this rule execution is associated with.
     */
    @Id
    @Column(name = "approval_request_id", nullable = false)
    private UUID approvalRequestId;

    /**
     * The ID of the moderation rule that was executed.
     */
    @Id
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    /**
     * The timestamp when the rule was executed.
     */
    @Column(name = "execution_timestamp", nullable = false)
    private LocalDateTime executionTimestamp;

    /**
     * The result of the rule execution (e.g., "PASS", "FAIL", "WARNING").
     */
    @Column(name = "result", nullable = false)
    private String result;

    /**
     * Additional details about the rule execution, such as specific violations found.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * The approval request entity this rule execution is associated with.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", referencedColumnName = "approval_request_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InfrastructureApprovalRequestJpaEntity approvalRequest;

    /**
     * The moderation rule entity that was executed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", referencedColumnName = "rule_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InfrastructureModerationRuleJpaEntity moderationRule;

    /**
     * Checks if the rule execution was successful.
     * 
     * @return true if the result indicates success (PASS)
     */
    public boolean isSuccessful() {
        return "PASS".equalsIgnoreCase(result);
    }

    /**
     * Checks if the rule execution failed.
     * 
     * @return true if the result indicates failure (FAIL)
     */
    public boolean isFailed() {
        return "FAIL".equalsIgnoreCase(result);
    }

    /**
     * Checks if the rule execution produced a warning.
     * 
     * @return true if the result indicates a warning (WARNING)
     */
    public boolean isWarning() {
        return "WARNING".equalsIgnoreCase(result);
    }

    /**
     * Gets a formatted string representation of the execution timestamp.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedExecutionTimestamp() {
        return executionTimestamp != null ? executionTimestamp.toString() : "N/A";
    }

    /**
     * Creates a summary of the rule execution for logging or reporting.
     * 
     * @return summary string
     */
    public String getExecutionSummary() {
        return String.format("Rule %s executed on request %s with result %s at %s",
                ruleId, approvalRequestId, result, getFormattedExecutionTimestamp());
    }

    /**
     * Validates the rule execution entity.
     * 
     * @return true if the entity is valid, false otherwise
     */
    public boolean isValid() {
        return approvalRequestId != null &&
               ruleId != null &&
               executionTimestamp != null &&
               result != null && !result.trim().isEmpty();
    }
}

/**
 * Composite primary key class for InfrastructureApprovalRequestRuleExecutionJpaEntity.
 * This class is required for JPA entities with composite primary keys.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class InfrastructureApprovalRequestRuleExecutionId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The approval request ID part of the composite key.
     */
    private UUID approvalRequestId;
    
    /**
     * The rule ID part of the composite key.
     */
    private UUID ruleId;

    /**
     * Validates the composite key.
     * 
     * @return true if the key is valid, false otherwise
     */
    public boolean isValid() {
        return approvalRequestId != null && ruleId != null;
    }

    /**
     * Creates a string representation of the composite key for logging or debugging.
     * 
     * @return formatted key string
     */
    public String getKeyString() {
        return String.format("[requestId=%s, ruleId=%s]", approvalRequestId, ruleId);
    }
}