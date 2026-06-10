package com.sustainabilitytracker.dtos.responses;

import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.EmissionScope;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class EmissionResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private Long submittedById;
    private String submittedByName;
    private Long approvedById;
    private String approvedByName;
    private BigDecimal co2Amount;
    private BigDecimal ch4Amount;
    private BigDecimal n2oAmount;
    private BigDecimal co2Equivalent;
    private EmissionScope scope;
    private DataStatus status;
    private String notes;
    private String rejectionReason;
    private LocalDate recordedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant createdAt;
}
