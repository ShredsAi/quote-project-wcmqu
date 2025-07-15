package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request parameters for moderator assignment endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedModeratorAssignmentRequestParams {
    @NotBlank(message = "Moderator ID is required")
    private String moderatorId;
}