package com.sustainabilitytracker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private BigDecimal environmentScore;
    private BigDecimal socialScore;
    private BigDecimal governanceScore;
    private BigDecimal totalScore;
    private String grade;
    private String periodType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Instant calculatedAt;
}
