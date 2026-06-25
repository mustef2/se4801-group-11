package com.sustainabilitytracker.dtos.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SocialRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Total workers is required")
    @Min(value = 0, message = "Total workers cannot be negative")
    private Integer totalWorkers;

    @Min(value = 0, message = "Female workers cannot be negative")
    private Integer femaleWorkers;

    @Min(value = 0, message = "Safety incidents cannot be negative")
    private Integer safetyIncidents;

    @Min(value = 0, message = "Training hours cannot be negative")
    private BigDecimal trainingHours;

    @DecimalMin(value = "0.0", message = "Satisfaction score cannot be negative")
    @DecimalMax(value = "100.0", message = "Satisfaction score cannot exceed 100")
    private BigDecimal satisfactionScore;

    private String notes;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedAt;
}
