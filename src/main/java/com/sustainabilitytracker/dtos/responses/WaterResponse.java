package com.sustainabilitytracker.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class WaterResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private BigDecimal consumedLiters;
    private BigDecimal recycledLiters;
    private String source;
    private String status;
    private String notes;
    private String rejectionReason;
    private LocalDate recordedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private String submittedByName;
    private String approvedByName;
}
