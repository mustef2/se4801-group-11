package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WasteSummaryResponse {
    private BigDecimal totalKg;
    private BigDecimal totalRecycledKg;
    private BigDecimal totalHazardousKg;
    private BigDecimal recyclingRate;        // percentage
    private Integer recordCount;
    private String period;
}
