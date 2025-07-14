package ai.shreds.infrastructure.repositories.jpa;

import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository interface for approval audit log entities.
 * Provides data access operations for audit logs.
 */
@Repository
public interface InfrastructureApprovalAuditLogJpaRepository extends JpaRepository<InfrastructureApprovalAuditLogJpaEntity, String> {
    
    /**
     * Finds audit logs by approval request ID, ordered by timestamp ascending.
     * 
     * @param approvalRequestId the approval request ID to filter by
     * @return list of audit logs for the request, ordered by timestamp
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByApprovalRequestIdOrderByTimestampAsc(String approvalRequestId);
    
    /**
     * Finds audit logs by approval request ID, ordered by timestamp descending.
     * 
     * @param approvalRequestId the approval request ID to filter by
     * @return list of audit logs for the request, ordered by timestamp descending
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByApprovalRequestIdOrderByTimestampDesc(String approvalRequestId);
    
    /**
     * Finds audit logs by performed by ID.
     * 
     * @param performedById the ID of the user who performed the actions
     * @return list of audit logs performed by the user
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByPerformedById(String performedById);
    
    /**
     * Finds audit logs by action type.
     * 
     * @param action the action type to filter by
     * @return list of audit logs for the action type
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByAction(String action);
    
    /**
     * Finds audit logs by timestamp range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of audit logs within the time range
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Finds audit logs by approval request ID and timestamp range.
     * 
     * @param approvalRequestId the approval request ID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of audit logs for the request within the time range
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByApprovalRequestIdAndTimestampBetween(String approvalRequestId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Finds audit logs by IP address.
     * 
     * @param ipAddress the IP address to filter by
     * @return list of audit logs from the IP address
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByIpAddress(String ipAddress);
    
    /**
     * Finds audit logs by approval request ID and action type.
     * 
     * @param approvalRequestId the approval request ID
     * @param action the action type
     * @return list of audit logs for the request and action type
     */
    List<InfrastructureApprovalAuditLogJpaEntity> findByApprovalRequestIdAndAction(String approvalRequestId, String action);
    
    /**
     * Finds the most recent audit log for a specific approval request.
     * 
     * @param approvalRequestId the approval request ID
     * @return the most recent audit log for the request if found
     */
    @Query("SELECT a FROM InfrastructureApprovalAuditLogJpaEntity a WHERE a.approvalRequestId = :approvalRequestId ORDER BY a.timestamp DESC LIMIT 1")
    Optional<InfrastructureApprovalAuditLogJpaEntity> findMostRecentByApprovalRequestId(@Param("approvalRequestId") String approvalRequestId);
    
    /**
     * Counts audit logs by approval request ID.
     * 
     * @param approvalRequestId the approval request ID
     * @return the count of audit logs for the request
     */
    long countByApprovalRequestId(String approvalRequestId);
    
    /**
     * Counts audit logs by action type.
     * 
     * @param action the action type
     * @return the count of audit logs for the action type
     */
    long countByAction(String action);
}