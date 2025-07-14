package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing an approval queue in the system.
 * Used for transferring approval queue data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedApprovalQueueDTO {
    @NotBlank
    private String queueId;
    
    @NotBlank
    private String queueName;
    
    @NotNull
    private Integer maxCapacity;
    
    @NotNull
    private Integer currentSize;
    
    @NotNull
    private Boolean isActive;
    
    @NotNull
    private String createdAt;
    
    private String lastProcessedAt;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Creates a DTO from a domain entity
     * 
     * @param entity The domain entity to convert
     * @return A new DTO representing the entity
     */
    public static SharedApprovalQueueDTO fromEntity(DomainApprovalQueueEntity entity) {
        return SharedApprovalQueueDTO.builder()
                .queueId(entity.getQueueId())
                .queueName(entity.getQueueName())
                .maxCapacity(entity.getMaxCapacity())
                .currentSize(entity.getCurrentSize())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt().format(DATE_FORMATTER))
                .lastProcessedAt(entity.getLastProcessedAt() != null ? entity.getLastProcessedAt().format(DATE_FORMATTER) : null)
                .build();
    }
    
    /**
     * Converts this DTO to a domain entity
     * 
     * @return The domain entity representation of this DTO
     */
    public DomainApprovalQueueEntity toEntity() {
        DomainApprovalQueueEntity entity = new DomainApprovalQueueEntity();
        entity.setQueueId(this.queueId);
        entity.setQueueName(this.queueName);
        entity.setMaxCapacity(this.maxCapacity);
        entity.setCurrentSize(this.currentSize);
        entity.setIsActive(this.isActive);
        entity.setCreatedAt(LocalDateTime.parse(this.createdAt, DATE_FORMATTER));
        entity.setLastProcessedAt(this.lastProcessedAt != null ? LocalDateTime.parse(this.lastProcessedAt, DATE_FORMATTER) : null);
        return entity;
    }
}