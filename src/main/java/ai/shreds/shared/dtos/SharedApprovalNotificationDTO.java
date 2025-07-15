package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an approval notification.
 * Used for transferring notification data between systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedApprovalNotificationDTO {
    @NotBlank
    private String type;
    
    @NotBlank
    private String recipientId;
    
    @NotBlank
    private String message;
    
    // Explicit getters and setters in case Lombok doesn't process
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}