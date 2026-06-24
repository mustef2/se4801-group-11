package com.sustainabilitytracker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergySummaryResponse {
    private BigDecimal totalKwh;
    private BigDecimal totalRenewableKwh;
    private BigDecimal averageKwh;
    private BigDecimal renewablePercentage;
    private String period;
    private Integer recordCount;
}
