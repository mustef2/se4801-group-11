package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull
    private Role role;
}
