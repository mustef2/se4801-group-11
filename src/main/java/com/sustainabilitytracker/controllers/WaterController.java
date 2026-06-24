package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.RejectRequest;
import com.sustainabilitytracker.dtos.requests.WaterRequest;
import com.sustainabilitytracker.dtos.responses.WaterResponse;
import com.sustainabilitytracker.dtos.responses.WaterSummaryResponse;
import com.sustainabilitytracker.repositories.WaterRepository;
import com.sustainabilitytracker.services.WaterService;
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
@RequestMapping("/water")
public class WaterController {

    private final WaterService waterService;
    private final WaterRepository waterRepository;

    @PostMapping
    public ResponseEntity<WaterResponse> submitWater(
            @Valid @RequestBody WaterRequest waterRequest,
            UriComponentsBuilder uriBuilder) {

        WaterResponse waterResponse = waterService.submitWater(waterRequest);

        var uri = uriBuilder.path("/water/{waterId}")
                .buildAndExpand(waterResponse.getId())
                .toUri();

        return ResponseEntity.created(uri).body(waterResponse);
    }

    // SUBMIT FOR APPROVAL
    @PutMapping("/{waterId}/submit")
    public ResponseEntity<WaterResponse> submitForApproval(@PathVariable Long waterId) {
        WaterResponse response = waterService.submitForApproval(waterId);
        return ResponseEntity.ok(response);
    }

    // APPROVE WATER
    @PutMapping("/{waterId}/approve")
    public ResponseEntity<WaterResponse> approveWater(@PathVariable Long waterId) {
        WaterResponse response = waterService.approveWater(waterId);
        return ResponseEntity.ok(response);
    }

    // REJECT WATER
    @PutMapping("/{waterId}/reject")
    public ResponseEntity<WaterResponse> rejectWater(
            @PathVariable Long waterId,
            @Valid @RequestBody RejectRequest request) {

        WaterResponse response = waterService.rejectWater(waterId, request.getReason());
        return ResponseEntity.ok(response);
    }

    // GET WATER BY COMPANY
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<WaterResponse>> getWaterByCompany(@PathVariable Long companyId) {
        List<WaterResponse> waterData = waterService.getWaterByCompany(companyId);
        return ResponseEntity.ok(waterData);
    }

    // GET WATER SUMMARY
    @GetMapping("/company/{companyId}/summary")
    public ResponseEntity<WaterSummaryResponse> getWaterSummary(
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

        WaterSummaryResponse summaryResponse = waterService
                .getWaterSummary(companyId, startDate, endDate);

        return ResponseEntity.ok(summaryResponse);
    }
}
