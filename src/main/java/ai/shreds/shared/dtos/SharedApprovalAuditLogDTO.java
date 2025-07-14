package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing an audit log entry for approval actions.
 * Used for transferring audit log data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedApprovalAuditLogDTO {
    @NotBlank
    private String auditId;
    
    @NotBlank
    private String approvalRequestId;
    
    @NotBlank
    private String action;
    
    @NotBlank
    private String performedById;
    
    private String oldValue;
    private String newValue;
    
    @NotNull
    private String timestamp;
    
    private String ipAddress;
    private String userAgent;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Creates a DTO from a domain entity
     * 
     * @param entity The domain entity to convert
     * @return A new DTO representing the entity
     */
    public static SharedApprovalAuditLogDTO fromEntity(DomainApprovalAuditLogEntity entity) {
        return SharedApprovalAuditLogDTO.builder()
                .auditId(entity.getAuditId())
                .approvalRequestId(entity.getApprovalRequestId())
                .action(entity.getAction())
                .performedById(entity.getPerformedById())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .timestamp(entity.getTimestamp().format(DATE_FORMATTER))
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .build();
    }
    
    /**
     * Converts this DTO to a domain entity
     * 
     * @return The domain entity representation of this DTO
     */
    public DomainApprovalAuditLogEntity toEntity() {
        DomainApprovalAuditLogEntity entity = new DomainApprovalAuditLogEntity();
        entity.setAuditId(this.auditId);
        entity.setApprovalRequestId(this.approvalRequestId);
        entity.setAction(this.action);
        entity.setPerformedById(this.performedById);
        entity.setOldValue(this.oldValue);
        entity.setNewValue(this.newValue);
        entity.setTimestamp(LocalDateTime.parse(this.timestamp, DATE_FORMATTER));
        entity.setIpAddress(this.ipAddress);
        entity.setUserAgent(this.userAgent);
        return entity;
    }
}