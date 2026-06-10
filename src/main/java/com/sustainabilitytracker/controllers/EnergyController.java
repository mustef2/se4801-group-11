package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.EnergyRequest;
import com.sustainabilitytracker.dtos.requests.RejectRequest;
import com.sustainabilitytracker.dtos.responses.EnergyResponse;
import com.sustainabilitytracker.dtos.responses.EnergySummaryResponse;
import com.sustainabilitytracker.services.EnergyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/energies")
public class EnergyController {

    private final EnergyService energyService;

    // SUBMIT ENERGY
    @PostMapping
    public ResponseEntity<EnergyResponse> submitEnergy(
            @Valid @RequestBody EnergyRequest energyRequest,
            UriComponentsBuilder uriBuilder) {

        EnergyResponse energyResponse = energyService.submitEnergy(energyRequest);

        var uri = uriBuilder.path("/energy/{energyId}")
                .buildAndExpand(energyResponse.getId())
                .toUri();

        return ResponseEntity.created(uri).body(energyResponse);
    }

    // SUBMIT FOR APPROVAL
    @PutMapping("/{energyId}/submit")
    public ResponseEntity<EnergyResponse> submitForApproval(@PathVariable Long energyId) {
        EnergyResponse energyResponse = energyService.submitForApproval(energyId);
        return ResponseEntity.ok(energyResponse);
    }

    // APPROVE ENERGY
    @PutMapping("/{energyId}/approve")
    public ResponseEntity<EnergyResponse> approveEnergy(@PathVariable Long energyId) {
        EnergyResponse energyResponse = energyService.approveEnergy(energyId);
        return ResponseEntity.ok(energyResponse);
    }

    // REJECT ENERGY
    @PutMapping("/{energyId}/reject")
    public ResponseEntity<EnergyResponse> rejectEnergy(
            @PathVariable Long energyId,
            @Valid @RequestBody RejectRequest request) {

        EnergyResponse energyResponse = energyService.rejectEnergy(energyId, request.getReason());
        return ResponseEntity.ok(energyResponse);
    }

    // GET ENERGY BY COMPANY
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<EnergyResponse>> getEnergyByCompany(@PathVariable Long companyId) {
        List<EnergyResponse> energyData = energyService.getEnergyByCompany(companyId);
        return ResponseEntity.ok(energyData);
    }

    // GET ENERGY SUMMARY
    @GetMapping("/company/{companyId}/summary")
    public ResponseEntity<EnergySummaryResponse> getEnergySummary(
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

        EnergySummaryResponse summaryResponse = energyService
                .getEnergySummary(companyId, startDate, endDate);

        return ResponseEntity.ok(summaryResponse);
    }
}
