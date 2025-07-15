package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import ai.shreds.infrastructure.mappers.InfrastructureEntityMapper;
import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalRequestJpaEntity;
import ai.shreds.infrastructure.repositories.jpa.InfrastructureApprovalRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class InfrastructureApprovalRequestRepositoryImpl implements DomainOutputPortApprovalRequestRepository {

    private final InfrastructureApprovalRequestJpaRepository jpaRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainApprovalRequestEntity save(DomainApprovalRequestEntity request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }
            
            log.debug("Saving approval request with ID: {}", request.getApprovalRequestId());
            
            InfrastructureApprovalRequestJpaEntity jpaEntity = entityMapper.toApprovalRequestJpa(request);
            InfrastructureApprovalRequestJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalRequestEntity result = entityMapper.toApprovalRequestDomain(savedEntity);
            log.debug("Successfully saved approval request with ID: {}", result.getApprovalRequestId());
            
            return result;
        } catch (Exception e) {
            log.error("Error saving approval request with ID: {}", request.getApprovalRequestId(), e);
            throw new InfrastructureRepositoryException("Failed to save approval request", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalRequestEntity> findById(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding approval request by ID: {}", requestId);
            
            return jpaRepository.findById(UUID.fromString(requestId))
                    .map(entityMapper::toApprovalRequestDomain);
        } catch (Exception e) {
            log.error("Error finding approval request with ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find approval request by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalRequestEntity> findPendingRequests(Map<String, Object> filter) {
        try {
            if (filter == null) {
                throw new IllegalArgumentException("Filter cannot be null");
            }
            
            log.debug("Finding pending requests with filter: {}", filter);
            
            String status = (String) filter.get("status");
            String priority = (String) filter.get("priority");
            String moderatorId = (String) filter.get("moderatorId");
            String queueId = (String) filter.get("queueId");
            
            UUID moderatorUUID = moderatorId != null ? UUID.fromString(moderatorId) : null;
            UUID queueUUID = queueId != null ? UUID.fromString(queueId) : null;
            
            List<InfrastructureApprovalRequestJpaEntity> jpaEntities = jpaRepository.findByFilters(
                status, priority, moderatorUUID, queueUUID
            );
            
            List<DomainApprovalRequestEntity> result = entityMapper.toApprovalRequestDomainList(jpaEntities);
            log.debug("Found {} pending requests", result.size());
            
            return result;
        } catch (Exception e) {
            log.error("Error finding pending requests with filter: {}", filter, e);
            throw new InfrastructureRepositoryException("Failed to find pending requests", e);
        }
    }

    @Override
    public DomainApprovalRequestEntity update(DomainApprovalRequestEntity request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }
            
            log.debug("Updating approval request with ID: {}", request.getApprovalRequestId());
            
            // Check if entity exists
            if (!jpaRepository.existsById(UUID.fromString(request.getApprovalRequestId()))) {
                log.error("Approval request not found for update with ID: {}", request.getApprovalRequestId());
                throw new InfrastructureRepositoryException("Approval request not found for update");
            }
            
            InfrastructureApprovalRequestJpaEntity jpaEntity = entityMapper.toApprovalRequestJpa(request);
            InfrastructureApprovalRequestJpaEntity updatedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalRequestEntity result = entityMapper.toApprovalRequestDomain(updatedEntity);
            log.debug("Successfully updated approval request with ID: {}", result.getApprovalRequestId());
            
            return result;
        } catch (Exception e) {
            log.error("Error updating approval request with ID: {}", request.getApprovalRequestId(), e);
            throw new InfrastructureRepositoryException("Failed to update approval request", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalRequestEntity> findByStatus(String status) {
        try {
            if (status == null) {
                throw new IllegalArgumentException("Status cannot be null");
            }
            
            log.debug("Finding approval requests by status: {}", status);
            
            List<InfrastructureApprovalRequestJpaEntity> jpaEntities = jpaRepository.findByStatus(status);
            List<DomainApprovalRequestEntity> result = entityMapper.toApprovalRequestDomainList(jpaEntities);
            
            log.debug("Found {} requests with status: {}", result.size(), status);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval requests by status: {}", status, e);
            throw new InfrastructureRepositoryException("Failed to find approval requests by status", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalRequestEntity> findByModeratorId(String moderatorId) {
        try {
            if (moderatorId == null || moderatorId.trim().isEmpty()) {
                throw new IllegalArgumentException("Moderator ID cannot be null or empty");
            }
            
            log.debug("Finding approval requests by moderator ID: {}", moderatorId);
            
            List<InfrastructureApprovalRequestJpaEntity> jpaEntities = jpaRepository.findByAssignedModeratorId(UUID.fromString(moderatorId));
            List<DomainApprovalRequestEntity> result = entityMapper.toApprovalRequestDomainList(jpaEntities);
            
            log.debug("Found {} requests assigned to moderator: {}", result.size(), moderatorId);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval requests by moderator ID: {}", moderatorId, e);
            throw new InfrastructureRepositoryException("Failed to find approval requests by moderator ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalRequestEntity> findByQuoteId(String quoteId) {
        try {
            if (quoteId == null || quoteId.trim().isEmpty()) {
                throw new IllegalArgumentException("Quote ID cannot be null or empty");
            }
            
            log.debug("Finding approval request by quote ID: {}", quoteId);
            
            return jpaRepository.findByQuoteId(UUID.fromString(quoteId))
                    .map(entityMapper::toApprovalRequestDomain);
        } catch (Exception e) {
            log.error("Error finding approval request with quote ID: {}", quoteId, e);
            throw new InfrastructureRepositoryException("Failed to find approval request by quote ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalRequestEntity> findByPriority(String priority) {
        try {
            if (priority == null) {
                throw new IllegalArgumentException("Priority cannot be null");
            }
            
            log.debug("Finding approval requests by priority: {}", priority);
            
            List<InfrastructureApprovalRequestJpaEntity> jpaEntities = jpaRepository.findByPriority(priority);
            List<DomainApprovalRequestEntity> result = entityMapper.toApprovalRequestDomainList(jpaEntities);
            
            log.debug("Found {} requests with priority: {}", result.size(), priority);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval requests by priority: {}", priority, e);
            throw new InfrastructureRepositoryException("Failed to find approval requests by priority", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalRequestEntity> findByQueueId(String queueId) {
        try {
            if (queueId == null || queueId.trim().isEmpty()) {
                throw new IllegalArgumentException("Queue ID cannot be null or empty");
            }
            
            log.debug("Finding approval requests by queue ID: {}", queueId);
            
            List<InfrastructureApprovalRequestJpaEntity> jpaEntities = jpaRepository.findByQueueId(UUID.fromString(queueId));
            List<DomainApprovalRequestEntity> result = entityMapper.toApprovalRequestDomainList(jpaEntities);
            
            log.debug("Found {} requests in queue: {}", result.size(), queueId);
            return result;
        } catch (Exception e) {
            log.error("Error finding approval requests by queue ID: {}", queueId, e);
            throw new InfrastructureRepositoryException("Failed to find approval requests by queue ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByQuoteId(String quoteId) {
        try {
            if (quoteId == null || quoteId.trim().isEmpty()) {
                throw new IllegalArgumentException("Quote ID cannot be null or empty");
            }
            
            log.debug("Checking existence of approval request by quote ID: {}", quoteId);
            
            return jpaRepository.existsByQuoteId(UUID.fromString(quoteId));
        } catch (Exception e) {
            log.error("Error checking existence of approval request by quote ID: {}", quoteId, e);
            throw new InfrastructureRepositoryException("Failed to check existence by quote ID", e);
        }
    }

    @Override
    public void deleteById(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Deleting approval request with ID: {}", requestId);
            
            jpaRepository.deleteById(UUID.fromString(requestId));
            
            log.debug("Successfully deleted approval request with ID: {}", requestId);
        } catch (Exception e) {
            log.error("Error deleting approval request with ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to delete approval request", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            log.debug("Deleting all approval requests");
            
            jpaRepository.deleteAll();
            
            log.debug("Successfully deleted all approval requests");
        } catch (Exception e) {
            log.error("Error deleting all approval requests", e);
            throw new InfrastructureRepositoryException("Failed to delete all approval requests", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            log.debug("Counting all approval requests");
            
            return jpaRepository.count();
        } catch (Exception e) {
            log.error("Error counting approval requests", e);
            throw new InfrastructureRepositoryException("Failed to count approval requests", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        try {
            if (status == null) {
                throw new IllegalArgumentException("Status cannot be null");
            }
            
            log.debug("Counting approval requests by status: {}", status);
            
            return jpaRepository.countByStatus(status);
        } catch (Exception e) {
            log.error("Error counting approval requests by status: {}", status, e);
            throw new InfrastructureRepositoryException("Failed to count approval requests by status", e);
        }
    }
}