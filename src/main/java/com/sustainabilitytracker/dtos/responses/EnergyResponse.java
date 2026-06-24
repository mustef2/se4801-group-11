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
public class EnergyResponse {
    private Long id;
    private String companyName;
    private String departmentName;
    private String submittedByName;
    private String approvedByName;
    private BigDecimal totalKwh;
    private BigDecimal renewableKwh;
    private String status;
    private String sourceType;
    private String notes;
    private String rejectionReason;
    private LocalDate recordedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant createdAt;
}
