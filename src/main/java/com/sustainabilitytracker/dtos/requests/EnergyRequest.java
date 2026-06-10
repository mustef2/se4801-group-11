package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.EnergySource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EnergyRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Total KWh is required")
    @Min(value = 0, message = "Total KWh cannot be negative")
    private BigDecimal totalKwh;

    @Min(value = 0, message = "Renewable KWh cannot be negative")
    private BigDecimal renewableKwh;

    @NotNull(message = "Source type is required")
    private EnergySource sourceType;

    private String notes;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedAt;
}