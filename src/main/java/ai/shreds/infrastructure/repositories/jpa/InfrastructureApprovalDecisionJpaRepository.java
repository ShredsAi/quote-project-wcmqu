package ai.shreds.infrastructure.repositories.jpa;

import ai.shreds.infrastructure.repositories.entities.InfrastructureApprovalDecisionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for approval decision entities.
 * Provides data access operations for approval decisions.
 */
@Repository
public interface InfrastructureApprovalDecisionJpaRepository extends JpaRepository<InfrastructureApprovalDecisionJpaEntity, UUID> {
    
    /**
     * Finds approval decisions by approval request ID.
     * 
     * @param approvalRequestId the approval request ID to filter by
     * @return list of approval decisions for the request
     */
    List<InfrastructureApprovalDecisionJpaEntity> findByApprovalRequestId(UUID approvalRequestId);
    
    /**
     * Finds approval decisions by moderator ID.
     * 
     * @param moderatorId the moderator ID to filter by
     * @return list of approval decisions made by the moderator
     */
    List<InfrastructureApprovalDecisionJpaEntity> findByModeratorId(UUID moderatorId);
    
    /**
     * Finds approval decisions by decision type.
     * 
     * @param decision the decision type to filter by
     * @return list of approval decisions of the specified type
     */
    List<InfrastructureApprovalDecisionJpaEntity> findByDecision(String decision);
    
    /**
     * Finds the latest decision for a specific approval request.
     * 
     * @param approvalRequestId the approval request ID
     * @return the latest decision for the request if found
     */
    @Query("SELECT d FROM InfrastructureApprovalDecisionJpaEntity d WHERE d.approvalRequestId = :approvalRequestId ORDER BY d.decisionTimestamp DESC LIMIT 1")
    Optional<InfrastructureApprovalDecisionJpaEntity> findLatestByApprovalRequestId(@Param("approvalRequestId") UUID approvalRequestId);
    
    /**
     * Checks if a decision exists for a given approval request.
     * 
     * @param approvalRequestId the approval request ID to check
     * @return true if a decision exists for the request
     */
    boolean existsByApprovalRequestId(UUID approvalRequestId);
    
    /**
     * Counts approval decisions by decision type.
     * 
     * @param decision the decision type to count
     * @return the count of decisions of the specified type
     */
    long countByDecision(String decision);
    
    /**
     * Counts approval decisions by moderator ID.
     * 
     * @param moderatorId the moderator ID to count
     * @return the count of decisions made by the moderator
     */
    long countByModeratorId(UUID moderatorId);
    
    /**
     * Counts approval decisions by moderator ID and decision type.
     * 
     * @param moderatorId the moderator ID to filter by
     * @param decision the decision type to filter by
     * @return the count of decisions matching the criteria
     */
    long countByModeratorIdAndDecision(UUID moderatorId, String decision);
    
    /**
     * Finds the average processing time for decisions made by a specific moderator.
     * 
     * @param moderatorId the moderator ID
     * @return the average processing time in milliseconds
     */
    @Query("SELECT AVG(d.processingTimeMs) FROM InfrastructureApprovalDecisionJpaEntity d WHERE d.moderatorId = :moderatorId")
    Double findAverageProcessingTimeByModerator(@Param("moderatorId") UUID moderatorId);
}