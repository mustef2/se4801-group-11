package com.sustainabilitytracker.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotNull
    private Long companyId;

    @NotBlank
    private String name;

    private String description;
}