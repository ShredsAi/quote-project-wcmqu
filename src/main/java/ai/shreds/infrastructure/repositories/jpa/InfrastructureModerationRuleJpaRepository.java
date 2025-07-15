package ai.shreds.infrastructure.repositories.jpa;

import ai.shreds.infrastructure.repositories.entities.InfrastructureModerationRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for moderation rule entities.
 * Provides data access operations for moderation rules.
 */
@Repository
public interface InfrastructureModerationRuleJpaRepository extends JpaRepository<InfrastructureModerationRuleJpaEntity, UUID> {
    
    /**
     * Finds all active moderation rules.
     * 
     * @return list of active moderation rules
     */
    List<InfrastructureModerationRuleJpaEntity> findByIsActiveTrue();
    
    /**
     * Finds moderation rule by name.
     * 
     * @param ruleName the name of the rule
     * @return the moderation rule if found
     */
    Optional<InfrastructureModerationRuleJpaEntity> findByRuleName(String ruleName);
    
    /**
     * Finds moderation rules by rule type.
     * 
     * @param ruleType the type of the rule
     * @return list of moderation rules of the specified type
     */
    List<InfrastructureModerationRuleJpaEntity> findByRuleType(String ruleType);
    
    /**
     * Finds active moderation rules by rule type.
     * 
     * @param ruleType the type of the rule
     * @return list of active moderation rules of the specified type
     */
    List<InfrastructureModerationRuleJpaEntity> findByRuleTypeAndIsActiveTrue(String ruleType);
    
    /**
     * Finds moderation rules by severity.
     * 
     * @param severity the severity level
     * @return list of moderation rules with the specified severity
     */
    List<InfrastructureModerationRuleJpaEntity> findBySeverity(String severity);
    
    /**
     * Finds active moderation rules by severity.
     * 
     * @param severity the severity level
     * @return list of active moderation rules with the specified severity
     */
    List<InfrastructureModerationRuleJpaEntity> findBySeverityAndIsActiveTrue(String severity);
    
    /**
     * Finds all active critical rules.
     * 
     * @return list of active critical moderation rules
     */
    @Query("SELECT r FROM InfrastructureModerationRuleJpaEntity r WHERE r.isActive = true AND r.severity = 'CRITICAL'")
    List<InfrastructureModerationRuleJpaEntity> findActiveCriticalRules();
    
    /**
     * Checks if a moderation rule with the given name exists.
     * 
     * @param ruleName the name to check
     * @return true if a rule with the name exists
     */
    boolean existsByRuleName(String ruleName);
    
    /**
     * Counts active moderation rules.
     * 
     * @return the count of active moderation rules
     */
    long countByIsActiveTrue();
    
    /**
     * Counts moderation rules by rule type.
     * 
     * @param ruleType the type of the rule
     * @return the count of moderation rules of the specified type
     */
    long countByRuleType(String ruleType);
    
    /**
     * Counts active moderation rules by rule type.
     * 
     * @param ruleType the type of the rule
     * @return the count of active moderation rules of the specified type
     */
    long countByRuleTypeAndIsActiveTrue(String ruleType);
    
    /**
     * Counts moderation rules by severity.
     * 
     * @param severity the severity level
     * @return the count of moderation rules with the specified severity
     */
    long countBySeverity(String severity);
}