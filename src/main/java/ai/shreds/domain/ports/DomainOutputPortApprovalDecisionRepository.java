package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainApprovalDecisionEntity;

import java.util.List;
import java.util.Optional;

/**
 * Domain output port for approval decision repository operations.
 * This interface defines the contract for persisting and retrieving approval decisions.
 * It is implemented by the infrastructure layer.
 */
public interface DomainOutputPortApprovalDecisionRepository {
    
    /**
     * Saves an approval decision entity to the repository.
     * 
     * @param decision the approval decision entity to save
     * @return the saved approval decision entity with any generated IDs
     * @throws IllegalArgumentException if the decision is null or invalid
     */
    DomainApprovalDecisionEntity save(DomainApprovalDecisionEntity decision);
    
    /**
     * Finds an approval decision by its ID.
     * 
     * @param decisionId the ID of the approval decision to find
     * @return the approval decision entity if found
     * @throws IllegalArgumentException if decisionId is null or empty
     */
    Optional<DomainApprovalDecisionEntity> findById(String decisionId);
    
    /**
     * Finds all approval decisions for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return a list of approval decision entities for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    List<DomainApprovalDecisionEntity> findByRequestId(String requestId);
    
    /**
     * Finds all approval decisions made by a specific moderator.
     * 
     * @param moderatorId the ID of the moderator
     * @return a list of approval decision entities made by the moderator
     * @throws IllegalArgumentException if moderatorId is null or empty
     */
    List<DomainApprovalDecisionEntity> findByModeratorId(String moderatorId);
    
    /**
     * Finds all approval decisions of a specific type.
     * 
     * @param decisionType the type of decision (e.g., "APPROVED", "REJECTED")
     * @return a list of approval decision entities of the specified type
     * @throws IllegalArgumentException if decisionType is null
     */
    List<DomainApprovalDecisionEntity> findByDecisionType(String decisionType);
    
    /**
     * Finds the latest decision for a specific approval request.
     * 
     * @param requestId the ID of the approval request
     * @return the latest approval decision entity for the request if found
     * @throws IllegalArgumentException if requestId is null or empty
     */
    Optional<DomainApprovalDecisionEntity> findLatestByRequestId(String requestId);
    
    /**
     * Updates an existing approval decision entity.
     * 
     * @param decision the approval decision entity to update
     * @return the updated approval decision entity
     * @throws IllegalArgumentException if the decision is null or invalid
     */
    DomainApprovalDecisionEntity update(DomainApprovalDecisionEntity decision);
    
    /**
     * Checks if a decision exists for a given approval request.
     * 
     * @param requestId the ID of the approval request
     * @return true if a decision exists for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    boolean existsByRequestId(String requestId);
    
    /**
     * Deletes an approval decision by its ID.
     * 
     * @param decisionId the ID of the approval decision to delete
     * @throws IllegalArgumentException if decisionId is null or empty
     */
    void deleteById(String decisionId);
    
    /**
     * Counts the total number of approval decisions.
     * 
     * @return the total count of approval decisions
     */
    long count();
    
    /**
     * Counts the number of approval decisions of a specific type.
     * 
     * @param decisionType the type of decision to count
     * @return the count of approval decisions of the specified type
     * @throws IllegalArgumentException if decisionType is null
     */
    long countByDecisionType(String decisionType);
    
    /**
     * Counts the number of approval decisions made by a specific moderator.
     * 
     * @param moderatorId the ID of the moderator
     * @return the count of approval decisions made by the moderator
     * @throws IllegalArgumentException if moderatorId is null or empty
     */
    long countByModeratorId(String moderatorId);
}