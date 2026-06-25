package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SocialSummaryResponse {
    private Integer totalWorkers;
    private Integer totalFemaleWorkers;
    private Integer totalSafetyIncidents;
    private BigDecimal averageTrainingHours;
    private BigDecimal averageSatisfactionScore;
    private Integer recordCount;
    private String period;
}
