package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GovernanceSummaryResponse {
    private BigDecimal averageComplianceScore;
    private Integer totalPolicies;
    private Integer totalViolations;
    private BigDecimal averageBoardDiversity;
    private Integer recordCount;
    private String period;
}
