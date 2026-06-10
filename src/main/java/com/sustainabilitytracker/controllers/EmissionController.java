package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.EmissionRequest;
import com.sustainabilitytracker.dtos.requests.RejectRequest;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.dtos.responses.EmissionSummaryResponse;
import com.sustainabilitytracker.services.EmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/emissions")
@AllArgsConstructor
public class EmissionController {
    private final EmissionService emissionService;

    @PostMapping
    public ResponseEntity<EmissionResponse> submitEmission(
            @Valid
            @RequestBody EmissionRequest emissionRequest,
            UriComponentsBuilder uriBuilder){
        EmissionResponse emissionResponse = emissionService.submitEmission(emissionRequest);
        var uri = uriBuilder.path("/emissions/{emissionsId}").buildAndExpand(emissionResponse.getId()).toUri();
        return ResponseEntity.created(uri).body(emissionResponse);
    }

    @PutMapping("/{emissionId}/submit")
    public ResponseEntity<EmissionResponse> submitForApproval(@PathVariable Long emissionId) {
        EmissionResponse emissionResponse = emissionService.submitForApproval(emissionId);
        return ResponseEntity.ok(emissionResponse);
    }

    @PutMapping("/{emissionId}/approve")
    public ResponseEntity<EmissionResponse> approveEmission(@PathVariable Long emissionId) {
        EmissionResponse emissionResponse = emissionService.approveEmission(emissionId);
        return ResponseEntity.ok(emissionResponse);
    }

    @PutMapping("/{emissionId}/reject")
    public ResponseEntity<EmissionResponse> rejectEmission(@PathVariable Long emissionId,
                                                           @Valid @RequestBody RejectRequest request) {
        EmissionResponse emissionResponse = emissionService.rejectEmission(emissionId,request.getReason());
        return ResponseEntity.ok(emissionResponse);
    }


}
