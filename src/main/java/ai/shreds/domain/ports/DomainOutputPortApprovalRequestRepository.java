package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainApprovalRequestEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain output port for approval request repository operations.
 * This interface defines the contract for persisting and retrieving approval requests.
 * It is implemented by the infrastructure layer.
 */
public interface DomainOutputPortApprovalRequestRepository {
    
    /**
     * Saves an approval request entity to the repository.
     * 
     * @param request the approval request entity to save
     * @return the saved approval request entity with any generated IDs
     * @throws IllegalArgumentException if the request is null or invalid
     */
    DomainApprovalRequestEntity save(DomainApprovalRequestEntity request);
    
    /**
     * Finds an approval request by its ID.
     * 
     * @param requestId the ID of the approval request to find
     * @return the approval request entity if found
     * @throws IllegalArgumentException if requestId is null or empty
     */
    Optional<DomainApprovalRequestEntity> findById(String requestId);
    
    /**
     * Finds all pending approval requests based on the provided filter criteria.
     * 
     * @param filter a map of filter criteria (e.g., "status", "priority", "moderatorId")
     * @return a list of approval request entities matching the filter
     * @throws IllegalArgumentException if filter is null
     */
    List<DomainApprovalRequestEntity> findPendingRequests(Map<String, Object> filter);
    
    /**
     * Updates an existing approval request entity.
     * 
     * @param request the approval request entity to update
     * @return the updated approval request entity
     * @throws IllegalArgumentException if the request is null or invalid
     */
    DomainApprovalRequestEntity update(DomainApprovalRequestEntity request);
    
    /**
     * Finds approval requests by their status.
     * 
     * @param status the status to filter by
     * @return a list of approval request entities with the specified status
     * @throws IllegalArgumentException if status is null
     */
    List<DomainApprovalRequestEntity> findByStatus(String status);
    
    /**
     * Finds approval requests assigned to a specific moderator.
     * 
     * @param moderatorId the ID of the moderator
     * @return a list of approval request entities assigned to the moderator
     * @throws IllegalArgumentException if moderatorId is null or empty
     */
    List<DomainApprovalRequestEntity> findByModeratorId(String moderatorId);
    
    /**
     * Finds approval requests by quote ID.
     * 
     * @param quoteId the ID of the quote
     * @return the approval request entity for the quote if found
     * @throws IllegalArgumentException if quoteId is null or empty
     */
    Optional<DomainApprovalRequestEntity> findByQuoteId(String quoteId);
    
    /**
     * Finds approval requests by priority level.
     * 
     * @param priority the priority level to filter by
     * @return a list of approval request entities with the specified priority
     * @throws IllegalArgumentException if priority is null
     */
    List<DomainApprovalRequestEntity> findByPriority(String priority);
    
    /**
     * Finds approval requests in a specific queue.
     * 
     * @param queueId the ID of the queue
     * @return a list of approval request entities in the specified queue
     * @throws IllegalArgumentException if queueId is null or empty
     */
    List<DomainApprovalRequestEntity> findByQueueId(String queueId);
    
    /**
     * Checks if an approval request exists for a given quote ID.
     * 
     * @param quoteId the ID of the quote
     * @return true if an approval request exists for the quote
     * @throws IllegalArgumentException if quoteId is null or empty
     */
    boolean existsByQuoteId(String quoteId);
    
    /**
     * Deletes an approval request by its ID.
     * 
     * @param requestId the ID of the approval request to delete
     * @throws IllegalArgumentException if requestId is null or empty
     */
    void deleteById(String requestId);
    
    /**
     * Counts the total number of approval requests.
     * 
     * @return the total count of approval requests
     */
    long count();
    
    /**
     * Counts the number of approval requests with a specific status.
     * 
     * @param status the status to count
     * @return the count of approval requests with the specified status
     * @throws IllegalArgumentException if status is null
     */
    long countByStatus(String status);
}