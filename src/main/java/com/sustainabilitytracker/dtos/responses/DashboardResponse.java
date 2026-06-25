package com.sustainabilitytracker.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    private Long companyId;
    private String companyName;
    private ScoreResponse latestScore;
    private List<ScoreResponse> scoreHistory;

    // Key Metrics
    private BigDecimal totalCo2;
    private BigDecimal totalEnergyKwh;
    private BigDecimal totalWaterLiters;
    private BigDecimal totalWasteKg;

    // Pending Items
    private Integer pendingApprovals;
    private Integer pendingReports;

    private Map<String, Object> targetsVsActual;
    private Integer unreadNotifications;
}