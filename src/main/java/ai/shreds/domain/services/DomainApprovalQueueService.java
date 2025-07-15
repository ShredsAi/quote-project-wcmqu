package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain service for managing approval queue operations and load balancing.
 * This service contains the core business logic for queue management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainApprovalQueueService {
    
    private final DomainOutputPortApprovalQueueRepository queueRepository;
    private final DomainOutputPortApprovalRequestRepository requestRepository;
    
    private static final double QUEUE_LOAD_THRESHOLD = 0.8; // 80% capacity threshold
    private static final int MAX_REBALANCE_ATTEMPTS = 3;
    
    /**
     * Adds an approval request to the appropriate queue based on priority and capacity.
     * 
     * @param request the approval request to add to a queue
     * @param queueId the preferred queue ID (optional)
     * @return the queue entity where the request was added
     * @throws IllegalArgumentException if the request is null or invalid
     * @throws IllegalStateException if no suitable queue is available
     */
    public DomainApprovalQueueEntity addToQueue(DomainApprovalRequestEntity request, String queueId) {
        validateRequest(request);
        
        DomainApprovalQueueEntity targetQueue;
        
        // If a specific queue is requested, try to use it
        if (queueId != null && !queueId.trim().isEmpty()) {
            Optional<DomainApprovalQueueEntity> specificQueue = queueRepository.findById(queueId);
            if (specificQueue.isPresent() && specificQueue.get().canAcceptRequest()) {
                targetQueue = specificQueue.get();
            } else {
                // Fall back to automatic queue selection
                targetQueue = selectOptimalQueue(request);
            }
        } else {
            // Automatic queue selection based on priority and load
            targetQueue = selectOptimalQueue(request);
        }
        
        // Add request to the selected queue
        targetQueue.incrementSize();
        DomainApprovalQueueEntity updatedQueue = queueRepository.update(targetQueue);
        
        // Update the request with the queue ID
        request.setQueueId(updatedQueue.getQueueId());
        requestRepository.update(request);
        
        log.info("Added request {} to queue {} (current size: {}/{})",
                request.getApprovalRequestId(), 
                updatedQueue.getQueueName(), 
                updatedQueue.getCurrentSize(), 
                updatedQueue.getMaxCapacity());
        
        return updatedQueue;
    }
    
    /**
     * Retrieves the next highest priority request from a queue for moderator assignment.
     * 
     * @param queueId the ID of the queue to get the next request from
     * @return the next approval request entity if available
     * @throws IllegalArgumentException if queueId is null or empty
     */
    public Optional<DomainApprovalRequestEntity> getNextRequest(String queueId) {
        validateQueueId(queueId);
        
        // Get all pending requests from the specified queue
        List<DomainApprovalRequestEntity> pendingRequests = requestRepository.findByQueueId(queueId)
                .stream()
                .filter(request -> request.getStatus() == DomainApprovalStatus.PENDING)
                .collect(Collectors.toList());
        
        if (pendingRequests.isEmpty()) {
            return Optional.empty();
        }
        
        // Sort by priority (highest first) and then by submission time (oldest first)
        DomainApprovalRequestEntity nextRequest = pendingRequests.stream()
                .sorted(Comparator
                        .comparing((DomainApprovalRequestEntity r) -> r.getPriority().getWeight())
                        .reversed() // Higher priority first
                        .thenComparing(DomainApprovalRequestEntity::getSubmittedAt)) // Older requests first
                .findFirst()
                .orElse(null);
        
        if (nextRequest != null) {
            log.debug("Retrieved next request {} from queue {} with priority {}", 
                    nextRequest.getApprovalRequestId(), queueId, nextRequest.getPriority());
        }
        
        return Optional.ofNullable(nextRequest);
    }
    
    /**
     * Redistributes pending requests across queues based on capacity and load balancing.
     * This method is executed as a scheduled background task.
     */
    public void rebalanceQueues() {
        log.info("Starting queue rebalancing process");
        
        List<DomainApprovalQueueEntity> activeQueues = queueRepository.findActiveQueues();
        if (activeQueues.isEmpty()) {
            log.warn("No active queues found for rebalancing");
            return;
        }
        
        // Find overloaded queues (above threshold)
        List<DomainApprovalQueueEntity> overloadedQueues = activeQueues.stream()
                .filter(queue -> queue.getUtilizationPercentage() > QUEUE_LOAD_THRESHOLD * 100)
                .collect(Collectors.toList());
        
        // Find underutilized queues (below threshold)
        List<DomainApprovalQueueEntity> underutilizedQueues = activeQueues.stream()
                .filter(queue -> queue.getUtilizationPercentage() < QUEUE_LOAD_THRESHOLD * 50) // 40% threshold
                .filter(DomainApprovalQueueEntity::canAcceptRequest)
                .collect(Collectors.toList());
        
        if (overloadedQueues.isEmpty() || underutilizedQueues.isEmpty()) {
            log.info("No rebalancing needed - overloaded: {}, underutilized: {}", 
                    overloadedQueues.size(), underutilizedQueues.size());
            return;
        }
        
        // Rebalance requests from overloaded to underutilized queues
        int totalRebalanced = 0;
        for (DomainApprovalQueueEntity overloadedQueue : overloadedQueues) {
            int rebalanced = rebalanceQueueRequests(overloadedQueue, underutilizedQueues);
            totalRebalanced += rebalanced;
        }
        
        log.info("Queue rebalancing completed. Rebalanced {} requests", totalRebalanced);
    }
    
    /**
     * Updates queue statistics after request processing.
     * 
     * @param queueId the ID of the queue to update
     * @throws IllegalArgumentException if queueId is null or empty
     */
    public void updateQueueStatistics(String queueId) {
        validateQueueId(queueId);
        
        Optional<DomainApprovalQueueEntity> queueOpt = queueRepository.findById(queueId);
        if (queueOpt.isEmpty()) {
            log.warn("Queue not found for statistics update: {}", queueId);
            return;
        }
        
        DomainApprovalQueueEntity queue = queueOpt.get();
        
        // Count actual pending requests in the queue
        long actualPendingCount = requestRepository.findByQueueId(queueId).stream()
                .filter(request -> request.getStatus() == DomainApprovalStatus.PENDING)
                .count();
        
        // Update the queue's current size if it doesn't match
        if (queue.getCurrentSize() != actualPendingCount) {
            log.info("Correcting queue {} size from {} to {}", 
                    queueId, queue.getCurrentSize(), actualPendingCount);
            queue.setCurrentSize((int) actualPendingCount);
            queueRepository.update(queue);
        }
        
        // Update last processed timestamp
        queue.setLastProcessedAt(LocalDateTime.now());
        queueRepository.update(queue);
        
        log.debug("Updated statistics for queue {}: size={}, utilization={}%", 
                queueId, queue.getCurrentSize(), queue.getUtilizationPercentage());
    }
    
    /**
     * Creates a new approval queue with the specified parameters.
     * 
     * @param queueName the name of the queue
     * @param maxCapacity the maximum capacity of the queue
     * @param isActive whether the queue should be active
     * @return the created queue entity
     * @throws IllegalArgumentException if parameters are invalid
     */
    public DomainApprovalQueueEntity createQueue(String queueName, int maxCapacity, boolean isActive) {
        validateQueueCreationParameters(queueName, maxCapacity);
        
        // Check if queue with same name already exists
        if (queueRepository.existsByName(queueName)) {
            throw new IllegalArgumentException("Queue with name already exists: " + queueName);
        }
        
        DomainApprovalQueueEntity queue = DomainApprovalQueueEntity.builder()
                .queueId(UUID.randomUUID().toString())
                .queueName(queueName)
                .maxCapacity(maxCapacity)
                .currentSize(0)
                .isActive(isActive)
                .createdAt(LocalDateTime.now())
                .build();
        
        queue.initializeQueue();
        DomainApprovalQueueEntity savedQueue = queueRepository.save(queue);
        
        log.info("Created new queue: {} with capacity {}", queueName, maxCapacity);
        
        return savedQueue;
    }
    
    /**
     * Gets queue performance metrics for monitoring and analysis.
     * 
     * @return a map of queue performance metrics
     */
    public Map<String, Object> getQueueMetrics() {
        List<DomainApprovalQueueEntity> allQueues = queueRepository.findAll();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalQueues", allQueues.size());
        metrics.put("activeQueues", allQueues.stream().filter(DomainApprovalQueueEntity::getIsActive).count());
        
        // Calculate average utilization
        double avgUtilization = allQueues.stream()
                .filter(DomainApprovalQueueEntity::getIsActive)
                .mapToDouble(DomainApprovalQueueEntity::getUtilizationPercentage)
                .average()
                .orElse(0.0);
        metrics.put("averageUtilization", avgUtilization);
        
        // Find most and least loaded queues
        Optional<DomainApprovalQueueEntity> mostLoaded = allQueues.stream()
                .filter(DomainApprovalQueueEntity::getIsActive)
                .max(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage));
        
        Optional<DomainApprovalQueueEntity> leastLoaded = allQueues.stream()
                .filter(DomainApprovalQueueEntity::getIsActive)
                .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage));
        
        mostLoaded.ifPresent(queue -> metrics.put("mostLoadedQueue", Map.of(
                "name", queue.getQueueName(),
                "utilization", queue.getUtilizationPercentage()
        )));
        
        leastLoaded.ifPresent(queue -> metrics.put("leastLoadedQueue", Map.of(
                "name", queue.getQueueName(),
                "utilization", queue.getUtilizationPercentage()
        )));
        
        return metrics;
    }
    
    /**
     * Selects the optimal queue for a new request based on priority and current load.
     * 
     * @param request the approval request to assign to a queue
     * @return the optimal queue for the request
     * @throws IllegalStateException if no suitable queue is available
     */
    private DomainApprovalQueueEntity selectOptimalQueue(DomainApprovalRequestEntity request) {
        List<DomainApprovalQueueEntity> availableQueues = queueRepository.findQueuesWithAvailableCapacity();
        
        if (availableQueues.isEmpty()) {
            throw new IllegalStateException("No queues with available capacity found");
        }
        
        // For urgent requests, prioritize low-utilization queues
        if (request.getPriority().isUrgent()) {
            return availableQueues.stream()
                    .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage))
                    .orElse(availableQueues.get(0));
        }
        
        // For high priority requests, try to find a specialized high-priority queue
        if (request.getPriority() == DomainPriority.HIGH) {
            Optional<DomainApprovalQueueEntity> highPriorityQueue = availableQueues.stream()
                    .filter(queue -> queue.getQueueName().toLowerCase().contains("high"))
                    .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage))
                    .or(() -> availableQueues.stream()
                            .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage)));
            
            if (highPriorityQueue.isPresent()) {
                return highPriorityQueue.get();
            }
        }
        
        // For normal and low priority requests, use the least loaded queue
        return availableQueues.stream()
                .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage))
                .orElse(availableQueues.get(0));
    }
    
    /**
     * Rebalances requests from an overloaded queue to underutilized queues.
     * 
     * @param overloadedQueue the queue that is overloaded
     * @param underutilizedQueues the list of underutilized queues
     * @return the number of requests that were rebalanced
     */
    private int rebalanceQueueRequests(DomainApprovalQueueEntity overloadedQueue, List<DomainApprovalQueueEntity> underutilizedQueues) {
        // Get pending requests from the overloaded queue (prioritize low-priority requests for moving)
        List<DomainApprovalRequestEntity> movableRequests = requestRepository.findByQueueId(overloadedQueue.getQueueId())
                .stream()
                .filter(request -> request.getStatus() == DomainApprovalStatus.PENDING)
                .filter(request -> request.getPriority() == DomainPriority.LOW || request.getPriority() == DomainPriority.NORMAL)
                .sorted(Comparator.comparing((DomainApprovalRequestEntity r) -> r.getPriority().getWeight())
                        .thenComparing(DomainApprovalRequestEntity::getSubmittedAt).reversed()) // Move newer requests first
                .collect(Collectors.toList());
        
        int rebalancedCount = 0;
        int maxToMove = Math.min(movableRequests.size(), overloadedQueue.getCurrentSize() / 4); // Move up to 25% of requests
        
        for (int i = 0; i < maxToMove && i < movableRequests.size() && rebalancedCount < MAX_REBALANCE_ATTEMPTS; i++) {
            DomainApprovalRequestEntity requestToMove = movableRequests.get(i);
            
            // Find the best underutilized queue for this request
            Optional<DomainApprovalQueueEntity> targetQueue = underutilizedQueues.stream()
                    .filter(DomainApprovalQueueEntity::canAcceptRequest)
                    .min(Comparator.comparingDouble(DomainApprovalQueueEntity::getUtilizationPercentage));
            
            if (targetQueue.isPresent()) {
                // Move the request
                String oldQueueId = requestToMove.getQueueId();
                requestToMove.setQueueId(targetQueue.get().getQueueId());
                requestRepository.update(requestToMove);
                
                // Update queue sizes
                overloadedQueue.decrementSize();
                targetQueue.get().incrementSize();
                
                queueRepository.update(overloadedQueue);
                queueRepository.update(targetQueue.get());
                
                rebalancedCount++;
                
                log.debug("Moved request {} from queue {} to queue {}", 
                        requestToMove.getApprovalRequestId(), 
                        oldQueueId, 
                        targetQueue.get().getQueueId());
            }
        }
        
        return rebalancedCount;
    }
    
    // Validation methods
    private void validateRequest(DomainApprovalRequestEntity request) {
        if (request == null) {
            throw new IllegalArgumentException("Approval request cannot be null");
        }
        if (request.getApprovalRequestId() == null || request.getApprovalRequestId().trim().isEmpty()) {
            throw new IllegalArgumentException("Approval request ID cannot be null or empty");
        }
    }
    
    private void validateQueueId(String queueId) {
        if (queueId == null || queueId.trim().isEmpty()) {
            throw new IllegalArgumentException("Queue ID cannot be null or empty");
        }
    }
    
    private void validateQueueCreationParameters(String queueName, int maxCapacity) {
        if (queueName == null || queueName.trim().isEmpty()) {
            throw new IllegalArgumentException("Queue name cannot be null or empty");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than 0");
        }
    }
}