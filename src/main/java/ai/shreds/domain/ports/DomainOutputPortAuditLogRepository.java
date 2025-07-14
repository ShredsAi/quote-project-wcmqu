package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain output port for audit log repository operations.
 * This interface defines the contract for persisting and retrieving audit logs.
 * It is implemented by the infrastructure layer.
 */
public interface DomainOutputPortAuditLogRepository {
    
    /**
     * Saves an audit log entity to the repository.
     * 
     * @param auditLog the audit log entity to save
     * @return the saved audit log entity with any generated IDs
     * @throws IllegalArgumentException if the auditLog is null or invalid
     */
    DomainApprovalAuditLogEntity save(DomainApprovalAuditLogEntity auditLog);
    
    /**
     * Finds an audit log by its ID.
     * 
     * @param auditId the ID of the audit log to find
     * @return the audit log entity if found
     * @throws IllegalArgumentException if auditId is null or empty
     */
    Optional<DomainApprovalAuditLogEntity> findById(String auditId);
    
    /**
     * Finds all audit logs for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return a list of audit log entities for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    List<DomainApprovalAuditLogEntity> findByRequestId(String requestId);
    
    /**
     * Finds all audit logs for a specific approval request, ordered by timestamp.
     * 
     * @param requestId the ID of the approval request
     * @return a list of audit log entities for the request, ordered by timestamp
     * @throws IllegalArgumentException if requestId is null or empty
     */
    List<DomainApprovalAuditLogEntity> findByRequestIdOrderByTimestamp(String requestId);
    
    /**
     * Finds all audit logs performed by a specific user.
     * 
     * @param performedById the ID of the user who performed the actions
     * @return a list of audit log entities for the user
     * @throws IllegalArgumentException if performedById is null or empty
     */
    List<DomainApprovalAuditLogEntity> findByPerformedById(String performedById);
    
    /**
     * Finds all audit logs for a specific action type.
     * 
     * @param action the action type to filter by
     * @return a list of audit log entities for the action type
     * @throws IllegalArgumentException if action is null or empty
     */
    List<DomainApprovalAuditLogEntity> findByAction(String action);
    
    /**
     * Finds all audit logs within a specific time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a list of audit log entities within the time range
     * @throws IllegalArgumentException if startTime or endTime is null
     */
    List<DomainApprovalAuditLogEntity> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Finds all audit logs for a specific approval request and action type.
     * 
     * @param requestId the ID of the approval request
     * @param action the action type to filter by
     * @return a list of audit log entities for the request and action type
     * @throws IllegalArgumentException if requestId or action is null or empty
     */
    List<DomainApprovalAuditLogEntity> findByRequestIdAndAction(String requestId, String action);
    
    /**
     * Finds the most recent audit log for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return the most recent audit log entity for the request if found
     * @throws IllegalArgumentException if requestId is null or empty
     */
    Optional<DomainApprovalAuditLogEntity> findMostRecentByRequestId(String requestId);
    
    /**
     * Counts the total number of audit logs.
     * 
     * @return the total count of audit logs
     */
    long count();
    
    /**
     * Counts the number of audit logs for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return the count of audit logs for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    long countByRequestId(String requestId);
    
    /**
     * Counts the number of audit logs for a specific action type.
     * 
     * @param action the action type to count
     * @return the count of audit logs for the action type
     * @throws IllegalArgumentException if action is null or empty
     */
    long countByAction(String action);
}