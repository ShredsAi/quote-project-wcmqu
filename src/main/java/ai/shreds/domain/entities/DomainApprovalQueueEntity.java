package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedApprovalQueueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing an approval queue for managing approval requests.
 * This entity encapsulates all the business rules related to queue management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainApprovalQueueEntity {
    private String queueId;
    private String queueName;
    private Integer maxCapacity;
    private Integer currentSize;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastProcessedAt;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Validates the queue data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the queue data is invalid
     */
    public void validate() {
        if (queueName == null || queueName.trim().isEmpty()) {
            throw new IllegalArgumentException("Queue name cannot be null or empty");
        }
        
        if (maxCapacity == null || maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than 0");
        }
        
        if (currentSize == null || currentSize < 0) {
            throw new IllegalArgumentException("Current size cannot be negative");
        }
        
        if (currentSize > maxCapacity) {
            throw new IllegalArgumentException("Current size cannot exceed max capacity");
        }
    }

    /**
     * Checks if the queue can accept a new request based on capacity.
     * 
     * @return true if the queue can accept a new request
     */
    public boolean canAcceptRequest() {
        return isActive && currentSize < maxCapacity;
    }

    /**
     * Increments the current size of the queue.
     * 
     * @throws IllegalStateException if the queue is at maximum capacity
     */
    public void incrementSize() {
        if (currentSize >= maxCapacity) {
            throw new IllegalStateException("Cannot increment size: queue is at maximum capacity");
        }
        this.currentSize++;
    }

    /**
     * Decrements the current size of the queue.
     * 
     * @throws IllegalStateException if the queue is already empty
     */
    public void decrementSize() {
        if (currentSize <= 0) {
            throw new IllegalStateException("Cannot decrement size: queue is already empty");
        }
        this.currentSize--;
        this.lastProcessedAt = LocalDateTime.now();
    }

    /**
     * Calculates the utilization percentage of the queue.
     * 
     * @return the utilization percentage (0-100)
     */
    public double getUtilizationPercentage() {
        if (maxCapacity == 0) {
            return 0.0;
        }
        return (double) currentSize / maxCapacity * 100.0;
    }

    /**
     * Calculates the available capacity in the queue.
     * 
     * @return the number of requests that can still be added
     */
    public int getAvailableCapacity() {
        return maxCapacity - currentSize;
    }

    /**
     * Checks if the queue is full.
     * 
     * @return true if the queue is at maximum capacity
     */
    public boolean isFull() {
        return currentSize >= maxCapacity;
    }

    /**
     * Checks if the queue is empty.
     * 
     * @return true if the queue has no requests
     */
    public boolean isEmpty() {
        return currentSize == 0;
    }

    /**
     * Activates the queue for processing requests.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the queue to stop processing requests.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Updates the queue statistics after processing a request.
     * 
     * @param wasProcessed true if a request was successfully processed
     */
    public void updateStatistics(boolean wasProcessed) {
        if (wasProcessed) {
            decrementSize();
        }
    }

    /**
     * Initializes the queue with default values.
     */
    public void initializeQueue() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (currentSize == null) {
            this.currentSize = 0;
        }
        if (isActive == null) {
            this.isActive = true;
        }
    }

    /**
     * Gets the formatted creation timestamp as a string.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_FORMATTER) : null;
    }

    /**
     * Gets the formatted last processed timestamp as a string.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedLastProcessedAt() {
        return lastProcessedAt != null ? lastProcessedAt.format(DATE_FORMATTER) : null;
    }

    /**
     * Converts this entity to a DTO for external communication.
     * 
     * @return a DTO representing this entity
     */
    public SharedApprovalQueueDTO toDTO() {
        return SharedApprovalQueueDTO.builder()
                .queueId(queueId)
                .queueName(queueName)
                .maxCapacity(maxCapacity)
                .currentSize(currentSize)
                .isActive(isActive)
                .createdAt(getFormattedCreatedAt())
                .lastProcessedAt(getFormattedLastProcessedAt())
                .build();
    }
}