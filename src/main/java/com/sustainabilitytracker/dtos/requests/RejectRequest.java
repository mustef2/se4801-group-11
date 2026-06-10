package com.sustainabilitytracker.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}