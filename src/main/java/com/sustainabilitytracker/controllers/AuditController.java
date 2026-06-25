package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.AuditRequest;
import com.sustainabilitytracker.dtos.responses.AuditResponse;
import com.sustainabilitytracker.dtos.responses.ReportResponse;
import com.sustainabilitytracker.services.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/pending")
    public ResponseEntity<List<ReportResponse>> getReportsForAudit() {
        List<ReportResponse> reports = auditService.getReportsForAudit();
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reports/{reportId}/review")
    public ResponseEntity<AuditResponse> reviewReport(
            @PathVariable Long reportId,
            @Valid @RequestBody AuditRequest request) {
        AuditResponse response = auditService
                .reviewReport(reportId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/{reportId}/history")
    public ResponseEntity<List<AuditResponse>> getAuditHistory(@PathVariable Long reportId) {
        List<AuditResponse> history = auditService.getAuditHistory(reportId);
        return ResponseEntity.ok(history);
    }
}

