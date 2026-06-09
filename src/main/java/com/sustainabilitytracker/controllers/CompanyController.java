package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.CompanyRequest;
import com.sustainabilitytracker.dtos.responses.CompanyResponse;
import com.sustainabilitytracker.services.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/companies")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public List<CompanyResponse> getAllCompanies(){
       return companyService.getAllCompanies();
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @RequestBody CompanyRequest request,
            UriComponentsBuilder uriBuilder){

        var companyResponse = companyService.createCompany(request);
        var uri = uriBuilder.path("api/v1/companies/{id}").buildAndExpand(companyResponse.getId()).toUri();

       return ResponseEntity.created(uri).body(companyResponse);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompanies(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyById(companyId));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @RequestBody CompanyRequest request,
            @PathVariable Long companyId) {

       CompanyResponse companyResponse = companyService.updateCompany(companyId, request);

       return ResponseEntity.ok(companyResponse);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        companyService.deactivateCompany(companyId);
        return ResponseEntity.noContent().build();
    }

}