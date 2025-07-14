package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import ai.shreds.infrastructure.mappers.InfrastructureEntityMapper;
import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalQueueJpaEntity;
import ai.shreds.infrastructure.repositories.jpa.InfrastructureApprovalQueueJpaRepository;
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
public class InfrastructureApprovalQueueRepositoryImpl implements DomainOutputPortApprovalQueueRepository {

    private final InfrastructureApprovalQueueJpaRepository jpaRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainApprovalQueueEntity save(DomainApprovalQueueEntity queue) {
        try {
            if (queue == null) {
                throw new IllegalArgumentException("Queue cannot be null");
            }
            
            log.debug("Saving approval queue with ID: {}", queue.getQueueId());
            
            InfrastructureApprovalQueueJpaEntity jpaEntity = entityMapper.toApprovalQueueJpa(queue);
            InfrastructureApprovalQueueJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalQueueEntity result = entityMapper.toApprovalQueueDomain(savedEntity);
            log.debug("Successfully saved approval queue with ID: {}", result.getQueueId());
            
            return result;
        } catch (Exception e) {
            log.error("Error saving approval queue with ID: {}", queue.getQueueId(), e);
            throw new InfrastructureRepositoryException("Failed to save approval queue", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalQueueEntity> findById(String queueId) {
        try {
            if (queueId == null || queueId.trim().isEmpty()) {
                throw new IllegalArgumentException("Queue ID cannot be null or empty");
            }
            
            log.debug("Finding approval queue by ID: {}", queueId);
            
            return jpaRepository.findById(queueId)
                    .map(entityMapper::toApprovalQueueDomain);
        } catch (Exception e) {
            log.error("Error finding approval queue with ID: {}", queueId, e);
            throw new InfrastructureRepositoryException("Failed to find approval queue by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalQueueEntity> findByName(String queueName) {
        try {
            if (queueName == null || queueName.trim().isEmpty()) {
                throw new IllegalArgumentException("Queue name cannot be null or empty");
            }
            
            log.debug("Finding approval queue by name: {}", queueName);
            
            return jpaRepository.findByQueueName(queueName)
                    .map(entityMapper::toApprovalQueueDomain);
        } catch (Exception e) {
            log.error("Error finding approval queue with name: {}", queueName, e);
            throw new InfrastructureRepositoryException("Failed to find approval queue by name", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalQueueEntity> findActiveQueues() {
        try {
            log.debug("Finding active approval queues");
            
            List<InfrastructureApprovalQueueJpaEntity> jpaEntities = jpaRepository.findByIsActiveTrue();
            List<DomainApprovalQueueEntity> result = entityMapper.toApprovalQueueDomainList(jpaEntities);
            
            log.debug("Found {} active approval queues", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding active approval queues", e);
            throw new InfrastructureRepositoryException("Failed to find active approval queues", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalQueueEntity> findAll() {
        try {
            log.debug("Finding all approval queues");
            
            List<InfrastructureApprovalQueueJpaEntity> jpaEntities = jpaRepository.findAll();
            List<DomainApprovalQueueEntity> result = entityMapper.toApprovalQueueDomainList(jpaEntities);
            
            log.debug("Found {} approval queues", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding all approval queues", e);
            throw new InfrastructureRepositoryException("Failed to find all approval queues", e);
        }
    }

    @Override
    public DomainApprovalQueueEntity update(DomainApprovalQueueEntity queue) {
        try {
            if (queue == null) {
                throw new IllegalArgumentException("Queue cannot be null");
            }
            
            log.debug("Updating approval queue with ID: {}", queue.getQueueId());
            
            // Check if entity exists
            if (!jpaRepository.existsById(queue.getQueueId())) {
                log.error("Approval queue not found for update with ID: {}", queue.getQueueId());
                throw new InfrastructureRepositoryException("Approval queue not found for update");
            }
            
            InfrastructureApprovalQueueJpaEntity jpaEntity = entityMapper.toApprovalQueueJpa(queue);
            InfrastructureApprovalQueueJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalQueueEntity result = entityMapper.toApprovalQueueDomain(updatedEntity);
            log.debug("Successfully updated approval queue with ID: {}", result.getQueueId());
            
            return result;
        } catch (Exception e) {
            log.error("Error updating approval queue with ID: {}", queue.getQueueId(), e);
            throw new InfrastructureRepositoryException("Failed to update approval queue", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalQueueEntity> findLeastLoadedQueue() {
        try {
            log.debug("Finding queue with least load");
            
            return jpaRepository.findLowestLoadQueue()
                    .map(entityMapper::toApprovalQueueDomain);
        } catch (Exception e) {
            log.error("Error finding queue with least load", e);
            throw new InfrastructureRepositoryException("Failed to find queue with least load", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalQueueEntity> findQueuesWithAvailableCapacity() {
        try {
            log.debug("Finding queues with available capacity");
            
            List<InfrastructureApprovalQueueJpaEntity> jpaEntities = jpaRepository.findQueuesWithAvailableCapacity();
            List<DomainApprovalQueueEntity> result = entityMapper.toApprovalQueueDomainList(jpaEntities);
            
            log.debug("Found {} queues with available capacity", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding queues with available capacity", e);
            throw new InfrastructureRepositoryException("Failed to find queues with available capacity", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalQueueEntity> findQueuesExceedingCapacityThreshold(double thresholdPercentage) {
        try {
            if (thresholdPercentage < 0) {
                throw new IllegalArgumentException("Threshold percentage cannot be negative");
            }
            
            log.debug("Finding queues exceeding capacity threshold: {}%", thresholdPercentage);
            
            List<InfrastructureApprovalQueueJpaEntity> jpaEntities = jpaRepository.findQueuesExceedingCapacityThreshold(thresholdPercentage);
            List<DomainApprovalQueueEntity> result = entityMapper.toApprovalQueueDomainList(jpaEntities);
            
            log.debug("Found {} queues exceeding capacity threshold", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding queues exceeding capacity threshold: {}", thresholdPercentage, e);
            throw new InfrastructureRepositoryException("Failed to find queues exceeding capacity threshold", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String queueName) {
        try {
            if (queueName == null || queueName.trim().isEmpty()) {
                throw new IllegalArgumentException("Queue name cannot be null or empty");
            }
            
            log.debug("Checking existence of queue by name: {}", queueName);
            
            return jpaRepository.existsByQueueName(queueName);
        } catch (Exception e) {
            log.error("Error checking existence of queue by name: {}", queueName, e);
            throw new InfrastructureRepositoryException("Failed to check existence by name", e);
        }
    }

    @Override
    public void deleteById(String queueId) {
        try {
            if (queueId == null || queueId.trim().isEmpty()) {
                throw new IllegalArgumentException("Queue ID cannot be null or empty");
            }
            
            log.debug("Deleting approval queue with ID: {}", queueId);
            
            jpaRepository.deleteById(queueId);
            
            log.debug("Successfully deleted approval queue with ID: {}", queueId);
        } catch (Exception e) {
            log.error("Error deleting approval queue with ID: {}", queueId, e);
            throw new InfrastructureRepositoryException("Failed to delete approval queue", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            log.debug("Counting all approval queues");
            
            return jpaRepository.count();
        } catch (Exception e) {
            log.error("Error counting approval queues", e);
            throw new InfrastructureRepositoryException("Failed to count approval queues", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        try {
            log.debug("Counting active approval queues");
            
            return jpaRepository.countByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error counting active approval queues", e);
            throw new InfrastructureRepositoryException("Failed to count active approval queues", e);
        }
    }
}