package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.RejectRequest;
import com.sustainabilitytracker.dtos.requests.SocialRequest;
import com.sustainabilitytracker.dtos.responses.SocialResponse;
import com.sustainabilitytracker.dtos.responses.SocialSummaryResponse;
import com.sustainabilitytracker.services.SocialService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping
    public ResponseEntity<SocialResponse> submitSocial(
            @Valid @RequestBody SocialRequest request,
            UriComponentsBuilder uriBuilder) {

        SocialResponse response = socialService.submitSocial(request);

        var uri = uriBuilder.path("/social/{socialId}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{socialId}/submit")
    public ResponseEntity<SocialResponse> submitForApproval(@PathVariable Long socialId) {
        SocialResponse response = socialService.submitForApproval(socialId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{socialId}/approve")
    public ResponseEntity<SocialResponse> approveSocial(@PathVariable Long socialId) {
        SocialResponse response = socialService.approveSocial(socialId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{socialId}/reject")
    public ResponseEntity<SocialResponse> rejectSocial(
            @PathVariable Long socialId,
            @Valid @RequestBody RejectRequest request) {

        SocialResponse response = socialService.rejectSocial(socialId, request.getReason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<SocialResponse>> getSocialByCompany(@PathVariable Long companyId) {
        List<SocialResponse> responses = socialService.getSocialByCompany(companyId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company/{companyId}/summary")
    public ResponseEntity<SocialSummaryResponse> getSocialSummary(
            @PathVariable Long companyId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        LocalDate now = LocalDate.now();
        LocalDate start = startDate != null ? startDate : now.minusDays(30);
        LocalDate end = endDate != null ? endDate : now;

        SocialSummaryResponse summary = socialService.getSocialSummary(companyId, start, end);

        return ResponseEntity.ok(summary);
    }
}

