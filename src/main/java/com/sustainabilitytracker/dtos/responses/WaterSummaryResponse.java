package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WaterSummaryResponse {
    private BigDecimal totalConsumedLiters;
    private BigDecimal totalRecycledLiters;
    private BigDecimal recyclingRate;
    private Integer recordCount;
    private String period;
}
