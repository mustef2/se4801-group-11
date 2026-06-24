package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.WaterSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WaterRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Consumed liters is required")
    @Min(value = 0, message = "Consumed liters cannot be negative")
    private BigDecimal consumedLiters;

    @Min(value = 0, message = "Recycled liters cannot be negative")
    private BigDecimal recycledLiters;

    @NotNull(message = "Water source is required")
    private WaterSource source;

    private String notes;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedAt;
}