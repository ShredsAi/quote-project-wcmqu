package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainApprovalQueueEntity;

import java.util.List;
import java.util.Optional;

/**
 * Domain output port for approval queue repository operations.
 * This interface defines the contract for persisting and retrieving approval queues.
 * It is implemented by the infrastructure layer.
 */
public interface DomainOutputPortApprovalQueueRepository {
    
    /**
     * Saves an approval queue entity to the repository.
     * 
     * @param queue the approval queue entity to save
     * @return the saved approval queue entity with any generated IDs
     * @throws IllegalArgumentException if the queue is null or invalid
     */
    DomainApprovalQueueEntity save(DomainApprovalQueueEntity queue);
    
    /**
     * Finds an approval queue by its ID.
     * 
     * @param queueId the ID of the approval queue to find
     * @return the approval queue entity if found
     * @throws IllegalArgumentException if queueId is null or empty
     */
    Optional<DomainApprovalQueueEntity> findById(String queueId);
    
    /**
     * Finds an approval queue by its name.
     * 
     * @param queueName the name of the approval queue to find
     * @return the approval queue entity if found
     * @throws IllegalArgumentException if queueName is null or empty
     */
    Optional<DomainApprovalQueueEntity> findByName(String queueName);
    
    /**
     * Finds all active approval queues.
     * 
     * @return a list of active approval queue entities
     */
    List<DomainApprovalQueueEntity> findActiveQueues();
    
    /**
     * Finds all approval queues.
     * 
     * @return a list of all approval queue entities
     */
    List<DomainApprovalQueueEntity> findAll();
    
    /**
     * Updates an existing approval queue entity.
     * 
     * @param queue the approval queue entity to update
     * @return the updated approval queue entity
     * @throws IllegalArgumentException if the queue is null or invalid
     */
    DomainApprovalQueueEntity update(DomainApprovalQueueEntity queue);
    
    /**
     * Finds the queue with the least current load (lowest utilization percentage).
     * 
     * @return the approval queue entity with the lowest load
     */
    Optional<DomainApprovalQueueEntity> findLeastLoadedQueue();
    
    /**
     * Finds queues that have available capacity.
     * 
     * @return a list of queues that can accept more requests
     */
    List<DomainApprovalQueueEntity> findQueuesWithAvailableCapacity();
    
    /**
     * Finds queues that have exceeded their capacity threshold.
     * 
     * @param thresholdPercentage the capacity threshold percentage (e.g., 80.0 for 80%)
     * @return a list of queues that have exceeded the threshold
     * @throws IllegalArgumentException if thresholdPercentage is negative
     */
    List<DomainApprovalQueueEntity> findQueuesExceedingCapacityThreshold(double thresholdPercentage);
    
    /**
     * Checks if a queue with the given name exists.
     * 
     * @param queueName the name to check
     * @return true if a queue with the name exists
     * @throws IllegalArgumentException if queueName is null or empty
     */
    boolean existsByName(String queueName);
    
    /**
     * Deletes an approval queue by its ID.
     * 
     * @param queueId the ID of the approval queue to delete
     * @throws IllegalArgumentException if queueId is null or empty
     */
    void deleteById(String queueId);
    
    /**
     * Deletes all approval queues from the repository.
     * This method is typically used for testing purposes.
     */
    void deleteAll();
    
    /**
     * Deletes a list of approval queues from the repository.
     * This method is typically used for testing purposes.
     * 
     * @param queues the list of approval queue entities to delete
     * @throws IllegalArgumentException if queues is null
     */
    void deleteAll(List<DomainApprovalQueueEntity> queues);
    
    /**
     * Counts the total number of approval queues.
     * 
     * @return the total count of approval queues
     */
    long count();
    
    /**
     * Counts the number of active approval queues.
     * 
     * @return the count of active approval queues
     */
    long countActive();
}