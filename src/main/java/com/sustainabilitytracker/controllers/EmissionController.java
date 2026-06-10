package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.EmissionRequest;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.services.EmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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


}
