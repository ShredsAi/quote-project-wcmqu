package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainModerationRuleEntity;

import java.util.List;
import java.util.Optional;

/**
 * Domain output port for moderation rule repository operations.
 * This interface defines the contract for persisting and retrieving moderation rules.
 * It is implemented by the infrastructure layer.
 */
public interface DomainOutputPortModerationRuleRepository {
    
    /**
     * Saves a moderation rule entity to the repository.
     * 
     * @param rule the moderation rule entity to save
     * @return the saved moderation rule entity with any generated IDs
     * @throws IllegalArgumentException if the rule is null or invalid
     */
    DomainModerationRuleEntity save(DomainModerationRuleEntity rule);
    
    /**
     * Finds a moderation rule by its ID.
     * 
     * @param ruleId the ID of the moderation rule to find
     * @return the moderation rule entity if found
     * @throws IllegalArgumentException if ruleId is null or empty
     */
    Optional<DomainModerationRuleEntity> findById(String ruleId);
    
    /**
     * Finds all active moderation rules.
     * 
     * @return a list of active moderation rule entities
     */
    List<DomainModerationRuleEntity> findActiveRules();
    
    /**
     * Finds all moderation rules.
     * 
     * @return a list of all moderation rule entities
     */
    List<DomainModerationRuleEntity> findAll();
    
    /**
     * Finds a moderation rule by its name.
     * 
     * @param ruleName the name of the moderation rule to find
     * @return the moderation rule entity if found
     * @throws IllegalArgumentException if ruleName is null or empty
     */
    Optional<DomainModerationRuleEntity> findByName(String ruleName);
    
    /**
     * Finds all moderation rules of a specific type.
     * 
     * @param ruleType the type of moderation rule to filter by
     * @return a list of moderation rule entities of the specified type
     * @throws IllegalArgumentException if ruleType is null
     */
    List<DomainModerationRuleEntity> findByRuleType(String ruleType);
    
    /**
     * Finds all moderation rules with a specific severity level.
     * 
     * @param severity the severity level to filter by
     * @return a list of moderation rule entities with the specified severity
     * @throws IllegalArgumentException if severity is null
     */
    List<DomainModerationRuleEntity> findBySeverity(String severity);
    
    /**
     * Finds all active moderation rules of a specific type.
     * 
     * @param ruleType the type of moderation rule to filter by
     * @return a list of active moderation rule entities of the specified type
     * @throws IllegalArgumentException if ruleType is null
     */
    List<DomainModerationRuleEntity> findActiveRulesByType(String ruleType);
    
    /**
     * Updates an existing moderation rule entity.
     * 
     * @param rule the moderation rule entity to update
     * @return the updated moderation rule entity
     * @throws IllegalArgumentException if the rule is null or invalid
     */
    DomainModerationRuleEntity update(DomainModerationRuleEntity rule);
    
    /**
     * Checks if a moderation rule with the given name exists.
     * 
     * @param ruleName the name to check
     * @return true if a rule with the name exists
     * @throws IllegalArgumentException if ruleName is null or empty
     */
    boolean existsByName(String ruleName);
    
    /**
     * Deletes a moderation rule by its ID.
     * 
     * @param ruleId the ID of the moderation rule to delete
     * @throws IllegalArgumentException if ruleId is null or empty
     */
    void deleteById(String ruleId);
    
    /**
     * Counts the total number of moderation rules.
     * 
     * @return the total count of moderation rules
     */
    long count();
    
    /**
     * Counts the number of active moderation rules.
     * 
     * @return the count of active moderation rules
     */
    long countActive();
    
    /**
     * Counts the number of moderation rules of a specific type.
     * 
     * @param ruleType the type of moderation rule to count
     * @return the count of moderation rules of the specified type
     * @throws IllegalArgumentException if ruleType is null
     */
    long countByRuleType(String ruleType);
}