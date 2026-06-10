package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.DepartmentRequest;
import com.sustainabilitytracker.dtos.responses.DepartmentResponse;
import com.sustainabilitytracker.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/departments")
@AllArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/{companyId}")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentByCompany(@PathVariable Long companyId) {
        List<DepartmentResponse> departments = departmentService.getDepartmentsByCompany(companyId);
        return ResponseEntity.ok(departments);
    }


    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            UriComponentsBuilder uriBuilder) {

        DepartmentResponse departmentResponse = departmentService.createDepartment(request);
        var uri = uriBuilder.path("api/v1/departments/{id}").buildAndExpand(departmentResponse.getId()).toUri();

        return ResponseEntity.created(uri).body(departmentResponse);
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @Valid @RequestBody DepartmentRequest request,
            @PathVariable Long departmentId) {
        DepartmentResponse departmentResponse = departmentService.updateDepartment(departmentId,request);
        return ResponseEntity.ok(departmentResponse);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> deactivateDepartment(@PathVariable Long departmentId){
        departmentService.deactivateDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }

}
