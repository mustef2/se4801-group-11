package com.sustainabilitytracker.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;

    @NotNull(message = "Password must be greater than 8.")
    @Size(min = 8)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}

