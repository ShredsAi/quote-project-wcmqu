package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a quote rejection event.
 * Used for transferring quote rejection event data between systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedQuoteRejectedEventDTO {
    @NotBlank
    private String eventType;
    
    @NotBlank
    private String quoteId;
    
    @NotBlank
    private String moderatorId;
    
    @NotNull
    private String timestamp;
    
    @NotBlank
    private String reason;
}