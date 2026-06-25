package com.sustainabilitytracker.sustainabilitytracker.controllers;

import com.sustainabilitytracker.sustainabilitytracker.dtos.request.RejectRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.request.WasteRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.WasteResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.WasteSummaryResponse;
import com.sustainabilitytracker.sustainabilitytracker.repositories.WasteRepository;
import com.sustainabilitytracker.sustainabilitytracker.services.WasteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/waste")
public class WasteController {

    private final WasteService wasteService;
    private final WasteRepository wasteRepository;

    @PostMapping
    public ResponseEntity<WasteResponse> submitWaste(
            @Valid @RequestBody WasteRequest wasteRequest,
            UriComponentsBuilder uriBuilder) {

        WasteResponse wasteResponse = wasteService.submitWaste(wasteRequest);

        var uri = uriBuilder.path("/waste/{wasteId}")
                .buildAndExpand(wasteResponse.getId())
                .toUri();

        return ResponseEntity.created(uri).body(wasteResponse);
    }

    @PutMapping("/{wasteId}/submit")
    public ResponseEntity<WasteResponse> submitForApproval(@PathVariable Long wasteId) {
        WasteResponse response = wasteService.submitForApproval(wasteId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{wasteId}/approve")
    public ResponseEntity<WasteResponse> approveWaste(@PathVariable Long wasteId) {
        WasteResponse response = wasteService.approveWaste(wasteId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{wasteId}/reject")
    public ResponseEntity<WasteResponse> rejectWaste(
            @PathVariable Long wasteId,
            @Valid @RequestBody RejectRequest request) {

        WasteResponse response = wasteService.rejectWaste(wasteId, request.getReason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<WasteResponse>> getWasteByCompany(@PathVariable Long companyId) {
        List<WasteResponse> wasteData = wasteService.getWasteByCompany(companyId);
        return ResponseEntity.ok(wasteData);
    }

    @GetMapping("/company/{companyId}/summary")
    public ResponseEntity<WasteSummaryResponse> getWasteSummary(
            @PathVariable Long companyId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        LocalDate now = LocalDate.now();

        Instant startInstant = (startDate != null ?
                startDate.atStartOfDay(ZoneOffset.UTC).toInstant() :
                now.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant());

        Instant endInstant = (endDate != null ?
                endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) :
                now.atTime(23, 59, 59).toInstant(ZoneOffset.UTC));

        WasteSummaryResponse summaryResponse = wasteService
                .getWasteSummary(companyId, startDate, endDate);

        return ResponseEntity.ok(summaryResponse);
    }
}
