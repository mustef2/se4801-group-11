package com.sustainabilitytracker.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmissionSummaryResponse {
    private BigDecimal totalCO2 = BigDecimal.ZERO;
    private BigDecimal totalCH4 = BigDecimal.ZERO;
    private BigDecimal totalN2O = BigDecimal.ZERO;
    private BigDecimal totalEmissions = BigDecimal.ZERO;
    private String period;
    private Integer recordCount = 0;
}
