package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.ReportRequest;
import com.sustainabilitytracker.dtos.responses.ReportResponse;
import com.sustainabilitytracker.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GENERATE REPORT
    @PostMapping
    public ResponseEntity<ReportResponse> generateReport(
            @Valid @RequestBody ReportRequest request,
            UriComponentsBuilder uriBuilder) {

        ReportResponse response = reportService.generateReport(request);

        var uri = uriBuilder
                .path("/reports/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    // GET REPORTS BY COMPANY
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ReportResponse>> getReportsByCompany(@PathVariable Long companyId) {
        List<ReportResponse> reports = reportService.getReportsByCompany(companyId);
        return ResponseEntity.ok(reports);
    }

    // DOWNLOAD REPORT
    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long reportId) {

        ReportResponse reportInfo = reportService
                .getReportById(reportId);

        byte[] fileBytes = reportService.downloadReport(reportId);

        // Dynamic content type
        boolean isExcel = "EXCEL".equalsIgnoreCase(
                reportInfo.getFileFormat()
        );

        MediaType mediaType = isExcel
                ? MediaType.parseMediaType(
                "application/vnd.openxmlformats-" +
                "officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;

        String fileName = isExcel
                ? "report.xlsx"
                : "report.pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .contentLength(fileBytes.length)
                .body(fileBytes);
    }
}

