package ai.shreds.infrastructure.repositories.jpa;

import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalQueueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository interface for approval queue entities.
 * Provides data access operations for approval queues.
 */
@Repository
public interface InfrastructureApprovalQueueJpaRepository extends JpaRepository<InfrastructureApprovalQueueJpaEntity, String> {
    
    /**
     * Finds all active approval queues.
     * 
     * @return list of active approval queues
     */
    List<InfrastructureApprovalQueueJpaEntity> findByIsActiveTrue();
    
    /**
     * Finds approval queue by name.
     * 
     * @param queueName the name of the queue
     * @return the approval queue if found
     */
    Optional<InfrastructureApprovalQueueJpaEntity> findByQueueName(String queueName);
    
    /**
     * Checks if a queue with the given name exists.
     * 
     * @param queueName the name to check
     * @return true if a queue with the name exists
     */
    boolean existsByQueueName(String queueName);
    
    /**
     * Counts the number of active approval queues.
     * 
     * @return the count of active approval queues
     */
    long countByIsActiveTrue();
    
    /**
     * Finds queues that have available capacity.
     * 
     * @return list of queues with available capacity
     */
    @Query("SELECT q FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true AND q.currentSize < q.maxCapacity")
    List<InfrastructureApprovalQueueJpaEntity> findQueuesWithAvailableCapacity();
    
    /**
     * Finds the queue with the lowest current size (least loaded).
     * 
     * @return the queue with the lowest load
     */
    @Query("SELECT q FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true AND q.currentSize < q.maxCapacity ORDER BY q.currentSize ASC LIMIT 1")
    Optional<InfrastructureApprovalQueueJpaEntity> findLowestLoadQueue();
    
    /**
     * Finds queues that have exceeded their capacity threshold.
     * 
     * @param thresholdPercentage the capacity threshold percentage
     * @return list of queues exceeding the threshold
     */
    @Query("SELECT q FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true AND (CAST(q.currentSize AS DOUBLE) / CAST(q.maxCapacity AS DOUBLE)) * 100 > :thresholdPercentage")
    List<InfrastructureApprovalQueueJpaEntity> findQueuesExceedingCapacityThreshold(@Param("thresholdPercentage") double thresholdPercentage);
    
    /**
     * Finds stale queues that haven't been processed recently.
     * 
     * @param cutoffTime the cutoff time for staleness
     * @return list of stale queues
     */
    @Query("SELECT q FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true AND (q.lastProcessedAt IS NULL OR q.lastProcessedAt < :cutoffTime)")
    List<InfrastructureApprovalQueueJpaEntity> findStaleQueues(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Calculates the total system capacity.
     * 
     * @return the total capacity of all active queues
     */
    @Query("SELECT SUM(q.maxCapacity) FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true")
    Integer calculateTotalSystemCapacity();
    
    /**
     * Calculates the total system load.
     * 
     * @return the total current size of all active queues
     */
    @Query("SELECT SUM(q.currentSize) FROM InfrastructureApprovalQueueJpaEntity q WHERE q.isActive = true")
    Integer calculateTotalSystemLoad();
}