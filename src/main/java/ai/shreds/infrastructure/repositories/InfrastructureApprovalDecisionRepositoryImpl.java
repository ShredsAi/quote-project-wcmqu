package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import ai.shreds.infrastructure.mappers.InfrastructureEntityMapper;
import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalDecisionJpaEntity;
import ai.shreds.infrastructure.repositories.jpa.InfrastructureApprovalDecisionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class InfrastructureApprovalDecisionRepositoryImpl implements DomainOutputPortApprovalDecisionRepository {

    private final InfrastructureApprovalDecisionJpaRepository jpaRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainApprovalDecisionEntity save(DomainApprovalDecisionEntity decision) {
        try {
            if (decision == null) {
                throw new IllegalArgumentException("Decision cannot be null");
            }
            
            log.debug("Saving approval decision with ID: {}", decision.getDecisionId());
            
            InfrastructureApprovalDecisionJpaEntity jpaEntity = entityMapper.toApprovalDecisionJpa(decision);
            InfrastructureApprovalDecisionJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalDecisionEntity result = entityMapper.toApprovalDecisionDomain(savedEntity);
            log.debug("Successfully saved approval decision with ID: {}", result.getDecisionId());
            
            return result;
        } catch (Exception e) {
            log.error("Error saving approval decision with ID: {}", decision.getDecisionId(), e);
            throw new InfrastructureRepositoryException("Failed to save approval decision", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalDecisionEntity> findById(String decisionId) {
        try {
            if (decisionId == null || decisionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Decision ID cannot be null or empty");
            }
            
            log.debug("Finding approval decision by ID: {}", decisionId);
            
            return jpaRepository.findById(UUID.fromString(decisionId))
                    .map(entityMapper::toApprovalDecisionDomain);
        } catch (Exception e) {
            log.error("Error finding approval decision by ID: {}", decisionId, e);
            throw new InfrastructureRepositoryException("Failed to find approval decision by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalDecisionEntity> findByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding approval decisions by request ID: {}", requestId);
            
            List<InfrastructureApprovalDecisionJpaEntity> jpaEntities = jpaRepository.findByApprovalRequestId(UUID.fromString(requestId));
            List<DomainApprovalDecisionEntity> result = entityMapper.toApprovalDecisionDomainList(jpaEntities);
            
            log.debug("Found {} decisions for request ID: {}", result.size(), requestId);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval decisions by request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find approval decisions by request ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalDecisionEntity> findByModeratorId(String moderatorId) {
        try {
            if (moderatorId == null || moderatorId.trim().isEmpty()) {
                throw new IllegalArgumentException("Moderator ID cannot be null or empty");
            }
            
            log.debug("Finding approval decisions by moderator ID: {}", moderatorId);
            
            List<InfrastructureApprovalDecisionJpaEntity> jpaEntities = jpaRepository.findByModeratorId(UUID.fromString(moderatorId));
            List<DomainApprovalDecisionEntity> result = entityMapper.toApprovalDecisionDomainList(jpaEntities);
            
            log.debug("Found {} decisions by moderator ID: {}", result.size(), moderatorId);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval decisions by moderator ID: {}", moderatorId, e);
            throw new InfrastructureRepositoryException("Failed to find approval decisions by moderator ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalDecisionEntity> findByDecisionType(String decisionType) {
        try {
            if (decisionType == null) {
                throw new IllegalArgumentException("Decision type cannot be null");
            }
            
            log.debug("Finding approval decisions by decision type: {}", decisionType);
            
            List<InfrastructureApprovalDecisionJpaEntity> jpaEntities = jpaRepository.findByDecision(decisionType);
            List<DomainApprovalDecisionEntity> result = entityMapper.toApprovalDecisionDomainList(jpaEntities);
            
            log.debug("Found {} decisions of type: {}", result.size(), decisionType);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval decisions by decision type: {}", decisionType, e);
            throw new InfrastructureRepositoryException("Failed to find approval decisions by decision type", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalDecisionEntity> findLatestByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding latest decision for request ID: {}", requestId);
            
            return jpaRepository.findLatestByApprovalRequestId(UUID.fromString(requestId))
                    .map(entityMapper::toApprovalDecisionDomain);
        } catch (Exception e) {
            log.error("Error finding latest decision for request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find latest decision for request ID", e);
        }
    }

    @Override
    public DomainApprovalDecisionEntity update(DomainApprovalDecisionEntity decision) {
        try {
            if (decision == null) {
                throw new IllegalArgumentException("Decision cannot be null");
            }
            
            log.debug("Updating approval decision with ID: {}", decision.getDecisionId());
            
            // Check if entity exists
            if (!jpaRepository.existsById(UUID.fromString(decision.getDecisionId()))) {
                log.error("Approval decision not found for update with ID: {}", decision.getDecisionId());
                throw new InfrastructureRepositoryException("Approval decision not found for update");
            }
            
            InfrastructureApprovalDecisionJpaEntity jpaEntity = entityMapper.toApprovalDecisionJpa(decision);
            InfrastructureApprovalDecisionJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalDecisionEntity result = entityMapper.toApprovalDecisionDomain(updatedEntity);
            log.debug("Successfully updated approval decision with ID: {}", result.getDecisionId());
            
            return result;
        } catch (Exception e) {
            log.error("Error updating approval decision with ID: {}", decision.getDecisionId(), e);
            throw new InfrastructureRepositoryException("Failed to update approval decision", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Checking existence of decision for request ID: {}", requestId);
            
            return jpaRepository.existsByApprovalRequestId(UUID.fromString(requestId));
        } catch (Exception e) {
            log.error("Error checking existence of decision for request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to check existence by request ID", e);
        }
    }

    @Override
    public void deleteById(String decisionId) {
        try {
            if (decisionId == null || decisionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Decision ID cannot be null or empty");
            }
            
            log.debug("Deleting approval decision with ID: {}", decisionId);
            
            jpaRepository.deleteById(UUID.fromString(decisionId));
            
            log.debug("Successfully deleted approval decision with ID: {}", decisionId);
        } catch (Exception e) {
            log.error("Error deleting approval decision with ID: {}", decisionId, e);
            throw new InfrastructureRepositoryException("Failed to delete approval decision", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            log.debug("Deleting all approval decisions");
            
            jpaRepository.deleteAll();
            
            log.debug("Successfully deleted all approval decisions");
        } catch (Exception e) {
            log.error("Error deleting all approval decisions", e);
            throw new InfrastructureRepositoryException("Failed to delete all approval decisions", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            log.debug("Counting all approval decisions");
            
            return jpaRepository.count();
        } catch (Exception e) {
            log.error("Error counting approval decisions", e);
            throw new InfrastructureRepositoryException("Failed to count approval decisions", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByDecisionType(String decisionType) {
        try {
            if (decisionType == null) {
                throw new IllegalArgumentException("Decision type cannot be null");
            }
            
            log.debug("Counting approval decisions by decision type: {}", decisionType);
            
            return jpaRepository.countByDecision(decisionType);
        } catch (Exception e) {
            log.error("Error counting approval decisions by decision type: {}", decisionType, e);
            throw new InfrastructureRepositoryException("Failed to count approval decisions by decision type", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByModeratorId(String moderatorId) {
        try {
            if (moderatorId == null || moderatorId.trim().isEmpty()) {
                throw new IllegalArgumentException("Moderator ID cannot be null or empty");
            }
            
            log.debug("Counting approval decisions by moderator ID: {}", moderatorId);
            
            return jpaRepository.countByModeratorId(UUID.fromString(moderatorId));
        } catch (Exception e) {
            log.error("Error counting approval decisions by moderator ID: {}", moderatorId, e);
            throw new InfrastructureRepositoryException("Failed to count approval decisions by moderator ID", e);
        }
    }
}