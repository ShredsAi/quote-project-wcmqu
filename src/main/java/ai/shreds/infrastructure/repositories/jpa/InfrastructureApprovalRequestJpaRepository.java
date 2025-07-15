package ai.shreds.infrastructure.repositories.jpa;

import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for approval request entities.
 * Provides data access operations for approval requests.
 */
@Repository
public interface InfrastructureApprovalRequestJpaRepository extends JpaRepository<InfrastructureApprovalRequestJpaEntity, UUID> {
    
    /**
     * Finds approval requests by status.
     * 
     * @param status the status to filter by
     * @return list of approval requests with the specified status
     */
    List<InfrastructureApprovalRequestJpaEntity> findByStatus(String status);
    
    /**
     * Finds approval requests by assigned moderator ID.
     * 
     * @param moderatorId the moderator ID to filter by
     * @return list of approval requests assigned to the moderator
     */
    List<InfrastructureApprovalRequestJpaEntity> findByAssignedModeratorId(UUID moderatorId);
    
    /**
     * Finds approval requests by priority and status.
     * 
     * @param priority the priority to filter by
     * @param status the status to filter by
     * @return list of approval requests matching the criteria
     */
    List<InfrastructureApprovalRequestJpaEntity> findByPriorityAndStatus(String priority, String status);
    
    /**
     * Finds approval request by quote ID.
     * 
     * @param quoteId the quote ID to search for
     * @return the approval request for the quote if found
     */
    Optional<InfrastructureApprovalRequestJpaEntity> findByQuoteId(UUID quoteId);
    
    /**
     * Finds approval requests by priority.
     * 
     * @param priority the priority to filter by
     * @return list of approval requests with the specified priority
     */
    List<InfrastructureApprovalRequestJpaEntity> findByPriority(String priority);
    
    /**
     * Finds approval requests by queue ID.
     * 
     * @param queueId the queue ID to filter by
     * @return list of approval requests in the specified queue
     */
    List<InfrastructureApprovalRequestJpaEntity> findByQueueId(UUID queueId);
    
    /**
     * Checks if an approval request exists for a given quote ID.
     * 
     * @param quoteId the quote ID to check
     * @return true if an approval request exists for the quote
     */
    boolean existsByQuoteId(UUID quoteId);
    
    /**
     * Counts approval requests by status.
     * 
     * @param status the status to count
     * @return the count of approval requests with the specified status
     */
    long countByStatus(String status);
    
    /**
     * Finds approval requests by multiple filter criteria.
     * 
     * @param status the status to filter by (optional)
     * @param priority the priority to filter by (optional)
     * @param moderatorId the moderator ID to filter by (optional)
     * @param queueId the queue ID to filter by (optional)
     * @return list of approval requests matching the criteria
     */
    @Query("SELECT ar FROM InfrastructureApprovalRequestJpaEntity ar WHERE " +
           "(:status IS NULL OR ar.status = :status) AND " +
           "(:priority IS NULL OR ar.priority = :priority) AND " +
           "(:moderatorId IS NULL OR ar.assignedModeratorId = :moderatorId) AND " +
           "(:queueId IS NULL OR ar.queueId = :queueId)")
    List<InfrastructureApprovalRequestJpaEntity> findByFilters(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("moderatorId") UUID moderatorId,
            @Param("queueId") UUID queueId);
}