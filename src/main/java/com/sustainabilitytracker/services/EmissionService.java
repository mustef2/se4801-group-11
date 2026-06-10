package com.sustainabilitytracker.services;


import com.sustainabilitytracker.dtos.requests.EmissionRequest;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.entities.EmissionData;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.mapper.EmissionMapper;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.repositories.EmissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@AllArgsConstructor
public class EmissionService {
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthService authService;
    private final EmissionRepository emissionRepository;
    private final EmissionMapper emissionMapper;

    public EmissionResponse submitEmission(EmissionRequest request) {

        // Validate company exists and is active
        Company company = companyRepository.findByIdAndIsActiveTrue(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found or inactive"));

        // Validate department exists and belongs to the company
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.getCompany().getId().equals(company.getId())) {
            throw new BusinessException("Department does not belong to the specified company");
        }

        // Get current user and check permission
        User currentUser = authService.getCurrentUser();

        boolean hasPermission = checkSubmissionPermission(currentUser, department, company);
        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission to submit emissions for this department");
        }

        // Check if record for same date/dept already exists and is APPROVED
        boolean alreadyApproved = emissionRepository.existsByDepartmentIdAndRecordedAtAndStatus(
                department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new BusinessException("Data already submitted and approved for this date");
        }

        // Check for abnormal values
        boolean hasWarning = isAbnormalValue(request.getCo2Amount());

        //  Map Request → Entity + enrich with business data
        EmissionData emissionData = emissionMapper.toEntity(request);

        emissionData.setCompany(company);
        emissionData.setDepartment(department);
        emissionData.setSubmittedBy(currentUser);
        emissionData.setStatus(DataStatus.DRAFT);
//        emissionData.setCreatedAt(Instant.now());
//        emissionData.setSubmittedAt(Instant.now()); // cuz i added @CreationTimestamp
//        emissionData.setHasWarning(hasWarning);

        EmissionData savedEmission = emissionRepository.save(emissionData);

        // Create notification for DEPT_MANAGER (optional but recommended)
        // notificationService.createEmissionSubmittedNotification(savedEmission);

        // Log action to system audit logs
        // auditLogService.logAction(currentUser.getId(), "EMISSION_SUBMITTED",
        //         "emission_data", savedEmission.getId(), "Submitted emission data");

//        EmissionResponse response = emissionMapper.toResponse(savedEmission);

//        response.setHasWarning(hasWarning);
//        response.setMessage(hasWarning
//                ? "Emission submitted successfully with warning: High CO₂ value detected"
//                : "Emission data submitted successfully");

        return emissionMapper.toResponse(savedEmission);
    }
    private boolean checkSubmissionPermission(User user, Department department, Company company) {
        // EMPLOYEE & DEPT_MANAGER → only their own department
        if (user.getRole() == Role.EMPLOYEE || user.getRole() == Role.DEPT_MANAGER) {
            return user.getDepartment().getId().equals(department.getId());
        }

        // SUSTAINABILITY_MANAGER → any department in their company
        if (user.getRole() == Role.SUSTAINABILITY_MANAGER) {
            return user.getCompany().getId().equals(company.getId());
        }

        return false;
    }
    private boolean isAbnormalValue(BigDecimal co2Amount) {
        final BigDecimal CO2_THRESHOLD = new BigDecimal("10000");
        return co2Amount != null && co2Amount.compareTo(CO2_THRESHOLD) > 0;
    }



}