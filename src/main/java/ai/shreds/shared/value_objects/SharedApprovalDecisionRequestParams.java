package ai.shreds.shared.value_objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedApprovalDecisionRequestParams {
    @NotBlank(message = "Decision is required")
    private String decision;
    private String reason;
    private String comments;
    @NotBlank(message = "Moderator ID is required")
    private String moderatorId;
}