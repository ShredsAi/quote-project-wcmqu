package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainModerationRuleEntity;
import ai.shreds.domain.ports.DomainOutputPortModerationRuleRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import ai.shreds.infrastructure.mappers.InfrastructureEntityMapper;
import ai.shreds.infrastructure.repositories.entities.InfrastructureModerationRuleJpaEntity;
import ai.shreds.infrastructure.repositories.jpa.InfrastructureModerationRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class InfrastructureModerationRuleRepositoryImpl implements DomainOutputPortModerationRuleRepository {

    private final InfrastructureModerationRuleJpaRepository jpaRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainModerationRuleEntity save(DomainModerationRuleEntity rule) {
        try {
            if (rule == null) {
                throw new IllegalArgumentException("Rule cannot be null");
            }
            
            log.debug("Saving moderation rule with ID: {}", rule.getRuleId());
            
            InfrastructureModerationRuleJpaEntity jpaEntity = entityMapper.toModerationRuleJpa(rule);
            InfrastructureModerationRuleJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            
            DomainModerationRuleEntity result = entityMapper.toModerationRuleDomain(savedEntity);
            log.debug("Successfully saved moderation rule with ID: {}", result.getRuleId());
            
            return result;
        } catch (Exception e) {
            log.error("Error saving moderation rule with ID: {}", rule.getRuleId(), e);
            throw new InfrastructureRepositoryException("Failed to save moderation rule", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainModerationRuleEntity> findById(String ruleId) {
        try {
            if (ruleId == null || ruleId.trim().isEmpty()) {
                throw new IllegalArgumentException("Rule ID cannot be null or empty");
            }
            
            log.debug("Finding moderation rule by ID: {}", ruleId);
            
            return jpaRepository.findById(ruleId)
                    .map(entityMapper::toModerationRuleDomain);
        } catch (Exception e) {
            log.error("Error finding moderation rule with ID: {}", ruleId, e);
            throw new InfrastructureRepositoryException("Failed to find moderation rule by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainModerationRuleEntity> findActiveRules() {
        try {
            log.debug("Finding active moderation rules");
            
            List<InfrastructureModerationRuleJpaEntity> jpaEntities = jpaRepository.findByIsActiveTrue();
            List<DomainModerationRuleEntity> result = entityMapper.toModerationRuleDomainList(jpaEntities);
            
            log.debug("Found {} active moderation rules", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding active moderation rules", e);
            throw new InfrastructureRepositoryException("Failed to find active moderation rules", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainModerationRuleEntity> findAll() {
        try {
            log.debug("Finding all moderation rules");
            
            List<InfrastructureModerationRuleJpaEntity> jpaEntities = jpaRepository.findAll();
            List<DomainModerationRuleEntity> result = entityMapper.toModerationRuleDomainList(jpaEntities);
            
            log.debug("Found {} moderation rules", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding all moderation rules", e);
            throw new InfrastructureRepositoryException("Failed to find all moderation rules", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainModerationRuleEntity> findByName(String ruleName) {
        try {
            if (ruleName == null || ruleName.trim().isEmpty()) {
                throw new IllegalArgumentException("Rule name cannot be null or empty");
            }
            
            log.debug("Finding moderation rule by name: {}", ruleName);
            
            return jpaRepository.findByRuleName(ruleName)
                    .map(entityMapper::toModerationRuleDomain);
        } catch (Exception e) {
            log.error("Error finding moderation rule with name: {}", ruleName, e);
            throw new InfrastructureRepositoryException("Failed to find moderation rule by name", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainModerationRuleEntity> findByRuleType(String ruleType) {
        try {
            if (ruleType == null) {
                throw new IllegalArgumentException("Rule type cannot be null");
            }
            
            log.debug("Finding moderation rules by type: {}", ruleType);
            
            List<InfrastructureModerationRuleJpaEntity> jpaEntities = jpaRepository.findByRuleType(ruleType);
            List<DomainModerationRuleEntity> result = entityMapper.toModerationRuleDomainList(jpaEntities);
            
            log.debug("Found {} moderation rules of type: {}", result.size(), ruleType);
            return result;
        } catch (Exception e) {
            log.error("Error finding moderation rules by type: {}", ruleType, e);
            throw new InfrastructureRepositoryException("Failed to find moderation rules by type", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainModerationRuleEntity> findBySeverity(String severity) {
        try {
            if (severity == null) {
                throw new IllegalArgumentException("Severity cannot be null");
            }
            
            log.debug("Finding moderation rules by severity: {}", severity);
            
            List<InfrastructureModerationRuleJpaEntity> jpaEntities = jpaRepository.findBySeverity(severity);
            List<DomainModerationRuleEntity> result = entityMapper.toModerationRuleDomainList(jpaEntities);
            
            log.debug("Found {} moderation rules with severity: {}", result.size(), severity);
            return result;
        } catch (Exception e) {
            log.error("Error finding moderation rules by severity: {}", severity, e);
            throw new InfrastructureRepositoryException("Failed to find moderation rules by severity", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainModerationRuleEntity> findActiveRulesByType(String ruleType) {
        try {
            if (ruleType == null) {
                throw new IllegalArgumentException("Rule type cannot be null");
            }
            
            log.debug("Finding active moderation rules by type: {}", ruleType);
            
            List<InfrastructureModerationRuleJpaEntity> jpaEntities = jpaRepository.findByRuleTypeAndIsActiveTrue(ruleType);
            List<DomainModerationRuleEntity> result = entityMapper.toModerationRuleDomainList(jpaEntities);
            
            log.debug("Found {} active moderation rules of type: {}", result.size(), ruleType);
            return result;
        } catch (Exception e) {
            log.error("Error finding active moderation rules by type: {}", ruleType, e);
            throw new InfrastructureRepositoryException("Failed to find active moderation rules by type", e);
        }
    }

    @Override
    public DomainModerationRuleEntity update(DomainModerationRuleEntity rule) {
        try {
            if (rule == null) {
                throw new IllegalArgumentException("Rule cannot be null");
            }
            
            log.debug("Updating moderation rule with ID: {}", rule.getRuleId());
            
            // Check if entity exists
            if (!jpaRepository.existsById(rule.getRuleId())) {
                log.error("Moderation rule not found for update with ID: {}", rule.getRuleId());
                throw new InfrastructureRepositoryException("Moderation rule not found for update");
            }
            
            InfrastructureModerationRuleJpaEntity jpaEntity = entityMapper.toModerationRuleJpa(rule);
            InfrastructureModerationRuleJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
            
            DomainModerationRuleEntity result = entityMapper.toModerationRuleDomain(updatedEntity);
            log.debug("Successfully updated moderation rule with ID: {}", result.getRuleId());
            
            return result;
        } catch (Exception e) {
            log.error("Error updating moderation rule with ID: {}", rule.getRuleId(), e);
            throw new InfrastructureRepositoryException("Failed to update moderation rule", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String ruleName) {
        try {
            if (ruleName == null || ruleName.trim().isEmpty()) {
                throw new IllegalArgumentException("Rule name cannot be null or empty");
            }
            
            log.debug("Checking existence of moderation rule by name: {}", ruleName);
            
            return jpaRepository.existsByRuleName(ruleName);
        } catch (Exception e) {
            log.error("Error checking existence of moderation rule by name: {}", ruleName, e);
            throw new InfrastructureRepositoryException("Failed to check existence by name", e);
        }
    }

    @Override
    public void deleteById(String ruleId) {
        try {
            if (ruleId == null || ruleId.trim().isEmpty()) {
                throw new IllegalArgumentException("Rule ID cannot be null or empty");
            }
            
            log.debug("Deleting moderation rule with ID: {}", ruleId);
            
            jpaRepository.deleteById(ruleId);
            
            log.debug("Successfully deleted moderation rule with ID: {}", ruleId);
        } catch (Exception e) {
            log.error("Error deleting moderation rule with ID: {}", ruleId, e);
            throw new InfrastructureRepositoryException("Failed to delete moderation rule", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            log.debug("Counting all moderation rules");
            
            return jpaRepository.count();
        } catch (Exception e) {
            log.error("Error counting moderation rules", e);
            throw new InfrastructureRepositoryException("Failed to count moderation rules", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        try {
            log.debug("Counting active moderation rules");
            
            return jpaRepository.countByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error counting active moderation rules", e);
            throw new InfrastructureRepositoryException("Failed to count active moderation rules", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRuleType(String ruleType) {
        try {
            if (ruleType == null) {
                throw new IllegalArgumentException("Rule type cannot be null");
            }
            
            log.debug("Counting moderation rules by type: {}", ruleType);
            
            return jpaRepository.countByRuleType(ruleType);
        } catch (Exception e) {
            log.error("Error counting moderation rules by type: {}", ruleType, e);
            throw new InfrastructureRepositoryException("Failed to count moderation rules by type", e);
        }
    }
}