package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a moderation assignment.
 * Used for transferring moderation assignment data between systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedModerationAssignmentDTO {
    @NotBlank
    private String requestId;
    
    @NotBlank
    private String moderatorId;
    
    @NotBlank
    @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT", message = "Priority must be one of: LOW, NORMAL, HIGH, URGENT")
    private String priority;
}