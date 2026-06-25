package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.WasteType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WasteRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Total kg is required")
    @Min(value = 0, message = "Total kg cannot be negative")
    private BigDecimal totalKg;

    @DecimalMin(value = "0.0", message = "Recycled kg cannot be negative")
    private BigDecimal recycledKg;

    @DecimalMin(value = "0.0", message = "Hazardous kg cannot be negative")
    private BigDecimal hazardousKg;

    @NotNull(message = "Waste type is required")
    private WasteType wasteType;

    private String notes;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedAt;

}