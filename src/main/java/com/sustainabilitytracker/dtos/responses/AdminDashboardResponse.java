package com.sustainabilitytracker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalCompanies;
    private long totalUsers;
    private ScoreResponse bestScore;
    private ScoreResponse worstScore;
    private SystemStatsResponse stats;
}