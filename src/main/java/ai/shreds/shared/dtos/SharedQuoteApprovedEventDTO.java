package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a quote approval event.
 * Used for transferring quote approval event data between systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedQuoteApprovedEventDTO {
    @NotBlank
    private String eventType;
    
    @NotBlank
    private String quoteId;
    
    @NotBlank
    private String moderatorId;
    
    @NotNull
    private String timestamp;
}