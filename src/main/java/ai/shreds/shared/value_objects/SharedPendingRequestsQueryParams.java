package ai.shreds.shared.value_objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedPendingRequestsQueryParams {
    private String moderatorId;
    
    @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT", message = "Priority must be one of: LOW, NORMAL, HIGH, URGENT")
    private String priority;
    
    private String queue;
}