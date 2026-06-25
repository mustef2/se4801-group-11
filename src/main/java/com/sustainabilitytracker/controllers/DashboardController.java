package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.responses.AdminDashboardResponse;
import com.sustainabilitytracker.dtos.responses.DashboardResponse;
import com.sustainabilitytracker.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/company/{companyId}")
    public ResponseEntity<DashboardResponse> getCompanyDashboard(@PathVariable Long companyId) {
        DashboardResponse dashboard = dashboardService.getCompanyDashboard(companyId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        AdminDashboardResponse dashboard = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }
}