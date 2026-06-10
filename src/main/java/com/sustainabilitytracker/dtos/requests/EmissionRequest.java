package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.EmissionScope;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmissionRequest {
    private Long companyId;
    private Long departmentId;

    @DecimalMin(value = "0.00")
    private BigDecimal co2Amount;

    @DecimalMin(value = "0.00")
    private BigDecimal ch4Amount;

    @DecimalMin(value = "0.00")
    private BigDecimal n2oAmount;

    @NotNull
    private EmissionScope scope;

    private String notes;

    @NotNull
    @PastOrPresent
    private LocalDate recordedAt;
}
