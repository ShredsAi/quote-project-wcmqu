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
}