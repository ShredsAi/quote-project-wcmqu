package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import ai.shreds.infrastructure.mappers.InfrastructureEntityMapper;
import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalAuditLogJpaEntity;
import ai.shreds.infrastructure.repositories.jpa.InfrastructureApprovalAuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class InfrastructureAuditLogRepositoryImpl implements DomainOutputPortAuditLogRepository {

    private final InfrastructureApprovalAuditLogJpaRepository jpaRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainApprovalAuditLogEntity save(DomainApprovalAuditLogEntity auditLog) {
        try {
            if (auditLog == null) {
                throw new IllegalArgumentException("Audit log cannot be null");
            }
            
            log.debug("Saving audit log with ID: {}", auditLog.getAuditId());
            
            InfrastructureApprovalAuditLogJpaEntity jpaEntity = entityMapper.toAuditLogJpa(auditLog);
            InfrastructureApprovalAuditLogJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            
            DomainApprovalAuditLogEntity result = entityMapper.toAuditLogDomain(savedEntity);
            log.debug("Successfully saved audit log with ID: {}", result.getAuditId());
            
            return result;
        } catch (Exception e) {
            log.error("Error saving audit log with ID: {}", auditLog.getAuditId(), e);
            throw new InfrastructureRepositoryException("Failed to save audit log", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalAuditLogEntity> findById(String auditId) {
        try {
            if (auditId == null || auditId.trim().isEmpty()) {
                throw new IllegalArgumentException("Audit ID cannot be null or empty");
            }
            
            log.debug("Finding audit log by ID: {}", auditId);
            
            return jpaRepository.findById(UUID.fromString(auditId))
                    .map(entityMapper::toAuditLogDomain);
        } catch (Exception e) {
            log.error("Error finding audit log by ID: {}", auditId, e);
            throw new InfrastructureRepositoryException("Failed to find audit log by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding audit logs by request ID: {}", requestId);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByApprovalRequestIdOrderByTimestampAsc(UUID.fromString(requestId));
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs for request ID: {}", result.size(), requestId);
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by request ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByRequestIdOrderByTimestamp(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding audit logs by request ID ordered by timestamp: {}", requestId);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByApprovalRequestIdOrderByTimestampAsc(UUID.fromString(requestId));
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs for request ID: {}", result.size(), requestId);
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by request ID ordered by timestamp: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by request ID ordered by timestamp", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByPerformedById(String performedById) {
        try {
            if (performedById == null || performedById.trim().isEmpty()) {
                throw new IllegalArgumentException("Performed by ID cannot be null or empty");
            }
            
            log.debug("Finding audit logs by performed by ID: {}", performedById);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByPerformedById(UUID.fromString(performedById));
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs performed by ID: {}", result.size(), performedById);
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by performed by ID: {}", performedById, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by performed by ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByAction(String action) {
        try {
            if (action == null || action.trim().isEmpty()) {
                throw new IllegalArgumentException("Action cannot be null or empty");
            }
            
            log.debug("Finding audit logs by action: {}", action);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByAction(action);
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs for action: {}", result.size(), action);
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by action: {}", action, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by action", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Start time and end time cannot be null");
            }
            
            log.debug("Finding audit logs by timestamp range: {} to {}", startTime, endTime);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByTimestampBetween(startTime, endTime);
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs in timestamp range", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by timestamp range: {} to {}", startTime, endTime, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by timestamp range", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainApprovalAuditLogEntity> findByRequestIdAndAction(String requestId, String action) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            if (action == null || action.trim().isEmpty()) {
                throw new IllegalArgumentException("Action cannot be null or empty");
            }
            
            log.debug("Finding audit logs by request ID {} and action: {}", requestId, action);
            
            List<InfrastructureApprovalAuditLogJpaEntity> jpaEntities = jpaRepository.findByApprovalRequestIdAndAction(UUID.fromString(requestId), action);
            List<DomainApprovalAuditLogEntity> result = entityMapper.toAuditLogDomainList(jpaEntities);
            
            log.debug("Found {} audit logs for request ID {} and action: {}", result.size(), requestId, action);
            return result;
        } catch (Exception e) {
            log.error("Error finding audit logs by request ID {} and action: {}", requestId, action, e);
            throw new InfrastructureRepositoryException("Failed to find audit logs by request ID and action", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainApprovalAuditLogEntity> findMostRecentByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Finding most recent audit log for request ID: {}", requestId);
            
            return jpaRepository.findMostRecentByApprovalRequestId(UUID.fromString(requestId))
                    .map(entityMapper::toAuditLogDomain);
        } catch (Exception e) {
            log.error("Error finding most recent audit log for request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to find most recent audit log for request ID", e);
        }
    }

    @Override
    public void deleteById(String auditId) {
        try {
            if (auditId == null || auditId.trim().isEmpty()) {
                throw new IllegalArgumentException("Audit ID cannot be null or empty");
            }
            
            log.debug("Deleting audit log with ID: {}", auditId);
            
            jpaRepository.deleteById(UUID.fromString(auditId));
            
            log.debug("Successfully deleted audit log with ID: {}", auditId);
        } catch (Exception e) {
            log.error("Error deleting audit log with ID: {}", auditId, e);
            throw new InfrastructureRepositoryException("Failed to delete audit log", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            log.debug("Deleting all audit logs");
            
            jpaRepository.deleteAll();
            
            log.debug("Successfully deleted all audit logs");
        } catch (Exception e) {
            log.error("Error deleting all audit logs", e);
            throw new InfrastructureRepositoryException("Failed to delete all audit logs", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            log.debug("Counting all audit logs");
            
            return jpaRepository.count();
        } catch (Exception e) {
            log.error("Error counting audit logs", e);
            throw new InfrastructureRepositoryException("Failed to count audit logs", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRequestId(String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("Request ID cannot be null or empty");
            }
            
            log.debug("Counting audit logs for request ID: {}", requestId);
            
            return jpaRepository.countByApprovalRequestId(UUID.fromString(requestId));
        } catch (Exception e) {
            log.error("Error counting audit logs for request ID: {}", requestId, e);
            throw new InfrastructureRepositoryException("Failed to count audit logs for request ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAction(String action) {
        try {
            if (action == null || action.trim().isEmpty()) {
                throw new IllegalArgumentException("Action cannot be null or empty");
            }
            
            log.debug("Counting audit logs by action: {}", action);
            
            return jpaRepository.countByAction(action);
        } catch (Exception e) {
            log.error("Error counting audit logs by action: {}", action, e);
            throw new InfrastructureRepositoryException("Failed to count audit logs by action", e);
        }
    }
}