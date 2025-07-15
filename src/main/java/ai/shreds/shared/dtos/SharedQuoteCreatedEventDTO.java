package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a quote creation event.
 * Used for transferring quote creation event data between systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedQuoteCreatedEventDTO {
    @NotBlank
    private String eventType;
    
    @NotBlank
    private String quoteId;
    
    @NotBlank
    private String submittedBy;
    
    @NotBlank
    @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT", message = "Priority must be one of: LOW, NORMAL, HIGH, URGENT")
    private String priority;
    
    @NotNull
    private String timestamp;
}