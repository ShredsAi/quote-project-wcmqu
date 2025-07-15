package ai.shreds.shared.value_objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query parameters for filtering approval queues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedQueuesQueryParams {
    private Boolean active;
    private String moderatorId;
}