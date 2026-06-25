package com.sustainabilitytracker.dtos.responses;

import com.sustainabilitytracker.enums.DataStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class SocialResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private Integer totalWorkers;
    private Integer femaleWorkers;
    private Integer safetyIncidents;
    private BigDecimal trainingHours;
    private BigDecimal satisfactionScore;
    private String status;
    private String notes;
    private String rejectionReason;
    private LocalDate recordedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private String submittedByName;
    private String approvedByName;
}
