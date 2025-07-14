package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing an audit log entry for approval actions.
 * Used for transferring audit log data between layers.
 */
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
    
    // Default constructor
    public SharedApprovalAuditLogDTO() {}
    
    // All args constructor
    public SharedApprovalAuditLogDTO(String auditId, String approvalRequestId, String action, 
                                    String performedById, String oldValue, String newValue, 
                                    String timestamp, String ipAddress, String userAgent) {
        this.auditId = auditId;
        this.approvalRequestId = approvalRequestId;
        this.action = action;
        this.performedById = performedById;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters
    public String getAuditId() { return auditId; }
    public String getApprovalRequestId() { return approvalRequestId; }
    public String getAction() { return action; }
    public String getPerformedById() { return performedById; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    
    // Setters
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }
    public void setAction(String action) { this.action = action; }
    public void setPerformedById(String performedById) { this.performedById = performedById; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String auditId;
        private String approvalRequestId;
        private String action;
        private String performedById;
        private String oldValue;
        private String newValue;
        private String timestamp;
        private String ipAddress;
        private String userAgent;
        
        public Builder auditId(String auditId) {
            this.auditId = auditId;
            return this;
        }
        
        public Builder approvalRequestId(String approvalRequestId) {
            this.approvalRequestId = approvalRequestId;
            return this;
        }
        
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        
        public Builder performedById(String performedById) {
            this.performedById = performedById;
            return this;
        }
        
        public Builder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }
        
        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }
        
        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public SharedApprovalAuditLogDTO build() {
            return new SharedApprovalAuditLogDTO(auditId, approvalRequestId, action, performedById, 
                                               oldValue, newValue, timestamp, ipAddress, userAgent);
        }
    }
    
    /**
     * Creates a DTO from a domain entity
     * 
     * @param entity The domain entity to convert
     * @return A new DTO representing the entity
     */
    public static SharedApprovalAuditLogDTO fromEntity(DomainApprovalAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SharedApprovalAuditLogDTO(
            entity.getAuditId(),
            entity.getApprovalRequestId(), 
            entity.getAction(),
            entity.getPerformedById(),
            entity.getOldValue(),
            entity.getNewValue(),
            entity.getTimestamp() != null ? entity.getTimestamp().format(DATE_FORMATTER) : null,
            entity.getIpAddress(),
            entity.getUserAgent()
        );
    }
    
    /**
     * Converts this DTO to a domain entity
     * 
     * @return The domain entity representation of this DTO
     */
    public DomainApprovalAuditLogEntity toEntity() {
        return DomainApprovalAuditLogEntity.builder()
            .auditId(this.auditId)
            .approvalRequestId(this.approvalRequestId)
            .action(this.action)
            .performedById(this.performedById)
            .oldValue(this.oldValue)
            .newValue(this.newValue)
            .timestamp(this.timestamp != null ? LocalDateTime.parse(this.timestamp, DATE_FORMATTER) : null)
            .ipAddress(this.ipAddress)
            .userAgent(this.userAgent)
            .build();
    }
}