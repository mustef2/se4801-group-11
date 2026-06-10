package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.EmissionScope;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmissionRequest {
    @NotNull
    private Long companyId;

    @NotNull
    private Long departmentId;

    @NotNull
    @Min(0)
    private BigDecimal co2Amount;

    @Min(0)
    private BigDecimal ch4Amount;

    @Min(0)
    private BigDecimal n2oAmount;

    @NotNull
    private EmissionScope scope;

    private String notes;

    @NotNull
    private LocalDate recordedAt;

}