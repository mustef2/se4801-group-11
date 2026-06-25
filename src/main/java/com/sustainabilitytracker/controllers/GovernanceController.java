package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.GovernanceRequest;
import com.sustainabilitytracker.dtos.requests.RejectRequest;
import com.sustainabilitytracker.dtos.responses.GovernanceResponse;
import com.sustainabilitytracker.dtos.responses.GovernanceSummaryResponse;
import com.sustainabilitytracker.services.GovernanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService governanceService;


    @PostMapping
    public ResponseEntity<GovernanceResponse> submitGovernance(
            @Valid @RequestBody GovernanceRequest request,
            UriComponentsBuilder uriBuilder) {

        GovernanceResponse response = governanceService.submitGovernance(request);

        var uri = uriBuilder.path("/api/v1/governance/{governanceId}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }


    @PutMapping("/{governanceId}/submit")
    public ResponseEntity<GovernanceResponse> submitForApproval(@PathVariable Long governanceId) {
        GovernanceResponse response = governanceService.submitForApproval(governanceId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{governanceId}/approve")
    public ResponseEntity<GovernanceResponse> approveGovernance(@PathVariable Long governanceId) {
        GovernanceResponse response = governanceService.approveGovernance(governanceId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{governanceId}/reject")
    public ResponseEntity<GovernanceResponse> rejectGovernance(
            @PathVariable Long governanceId,
            @Valid @RequestBody RejectRequest request) {

        GovernanceResponse response = governanceService.rejectGovernance(governanceId, request.getReason());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<GovernanceResponse>> getGovernanceByCompany(@PathVariable Long companyId) {
        List<GovernanceResponse> responses = governanceService.getGovernanceByCompany(companyId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/company/{companyId}/summary")
    public ResponseEntity<GovernanceSummaryResponse> getGovernanceSummary(
            @PathVariable Long companyId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        LocalDate now = LocalDate.now();
        LocalDate start = startDate != null ? startDate : now.minusDays(30);
        LocalDate end = endDate != null ? endDate : now;

        GovernanceSummaryResponse summary = governanceService.getGovernanceSummary(companyId, start, end);

        return ResponseEntity.ok(summary);package com.sustainabilitytracker.sustainabilitytracker.controllers;

import com.sustainabilitytracker.sustainabilitytracker.dtos.request.GovernanceRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.request.RejectRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.GovernanceResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.GovernanceSummaryResponse;
import com.sustainabilitytracker.sustainabilitytracker.services.GovernanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

        @RestController
        @RequestMapping("/governance")
        @RequiredArgsConstructor
        public class GovernanceController {

            private final GovernanceService governanceService;


            @PostMapping
            public ResponseEntity<GovernanceResponse> submitGovernance(
                    @Valid @RequestBody GovernanceRequest request,
                    UriComponentsBuilder uriBuilder) {

                GovernanceResponse response = governanceService.submitGovernance(request);

                var uri = uriBuilder.path("/api/v1/governance/{governanceId}")
                        .buildAndExpand(response.getId())
                        .toUri();

                return ResponseEntity.created(uri).body(response);
            }


            @PutMapping("/{governanceId}/submit")
            public ResponseEntity<GovernanceResponse> submitForApproval(@PathVariable Long governanceId) {
                GovernanceResponse response = governanceService.submitForApproval(governanceId);
                return ResponseEntity.ok(response);
            }


            @PutMapping("/{governanceId}/approve")
            public ResponseEntity<GovernanceResponse> approveGovernance(@PathVariable Long governanceId) {
                GovernanceResponse response = governanceService.approveGovernance(governanceId);
                return ResponseEntity.ok(response);
            }


            @PutMapping("/{governanceId}/reject")
            public ResponseEntity<GovernanceResponse> rejectGovernance(
                    @PathVariable Long governanceId,
                    @Valid @RequestBody RejectRequest request) {

                GovernanceResponse response = governanceService.rejectGovernance(governanceId, request.getReason());
                return ResponseEntity.ok(response);
            }


            @GetMapping("/company/{companyId}")
            public ResponseEntity<List<GovernanceResponse>> getGovernanceByCompany(@PathVariable Long companyId) {
                List<GovernanceResponse> responses = governanceService.getGovernanceByCompany(companyId);
                return ResponseEntity.ok(responses);
            }


            @GetMapping("/company/{companyId}/summary")
            public ResponseEntity<GovernanceSummaryResponse> getGovernanceSummary(
                    @PathVariable Long companyId,
                    @RequestParam(required = false) LocalDate startDate,
                    @RequestParam(required = false) LocalDate endDate) {

                LocalDate now = LocalDate.now();
                LocalDate start = startDate != null ? startDate : now.minusDays(30);
                LocalDate end = endDate != null ? endDate : now;

                GovernanceSummaryResponse summary = governanceService.getGovernanceSummary(companyId, start, end);

                return ResponseEntity.ok(summary);
            }
        }

    }
}

