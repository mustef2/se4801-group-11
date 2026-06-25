package com.sustainabilitytracker.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class GovernanceResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private BigDecimal complianceScore;
    private Integer policyCount;
    private Integer violationsCount;
    private BigDecimal boardDiversityPct;
    private Boolean ethicsTrainingDone;
    private String status;
    private String notes;
    private String rejectionReason;
    private LocalDate recordedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private String submittedByName;
    private String approvedByName;
}
