package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EmissionSummaryResponse {
    private Long companyId;
    private String period;
    private long recordCount;
    private BigDecimal totalCo2Amount;
    private BigDecimal totalCh4Amount;
    private BigDecimal totalN2oAmount;
    private BigDecimal totalCo2Equivalent;
}
