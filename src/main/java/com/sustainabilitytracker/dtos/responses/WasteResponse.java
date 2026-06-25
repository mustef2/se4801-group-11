package com.sustainabilitytracker.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class WasteResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String submittedByName;
    private String approvedByName;
    private Long departmentId;
    private String departmentName;

    private BigDecimal totalKg;
    private BigDecimal recycledKg;
    private BigDecimal hazardousKg;

    private String wasteType;
    private String status;
    private String notes;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant approvedAt;
    private LocalDate recordedAt;
}
