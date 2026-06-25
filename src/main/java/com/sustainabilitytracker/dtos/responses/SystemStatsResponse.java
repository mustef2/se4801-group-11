package com.sustainabilitytracker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {
    private long totalCompanies;
    private long totalUsers;
    private long totalReports;
    private long pendingAudits;
    private long activeCompanies;
}