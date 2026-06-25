package com.sustainabilitytracker.dtos.responses;

import com.sustainabilitytracker.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private Long id;
    private Long reportId;
    private String reportTitle;
    private Long auditorId;
    private String auditorName;
    private AuditAction action;
    private String comments;
    private String flaggedItems;
    private Instant createdAt;
}