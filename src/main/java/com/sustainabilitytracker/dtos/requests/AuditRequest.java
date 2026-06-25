package com.sustainabilitytracker.dtos.requests;

//import com.sustainabilitytracker.sustainabilitytracker.enums.AuditAction;
import com.sustainabilitytracker.enums.AuditAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequest {

    @NotNull(message = "Action is required")
    private AuditAction action;

    @NotBlank(message = "Comments are required")
    @Size(min = 10, max = 1000,
            message = "Comments must be between 10 and 1000 characters")
    private String comments;

    // Required only when action = FLAGGED
    // Validated in service layer (conditional validation)
    private String flaggedItems;
}