package com.sustainabilitytracker.services;


import com.sustainabilitytracker.dtos.requests.EmissionRequest;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.dtos.responses.EmissionSummaryResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


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

    @Transactional
    public EmissionResponse submitForApproval(Long emissionId) {

        EmissionData emissionData = emissionRepository.findById(emissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Emission not found with id: " + emissionId));

        User currentUser = authService.getCurrentUser();

        if (!emissionData.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only submit your own emission records for approval");
        }

        if (emissionData.getStatus() != DataStatus.DRAFT) {
            throw new BusinessException("Only DRAFT records can be submitted for approval. Current status: "
                    + emissionData.getStatus());
        }

        emissionData.setStatus(DataStatus.PENDING);
        emissionData.setSubmittedAt(Instant.now());
        EmissionData savedEmission = emissionRepository.save(emissionData);

        // Send notification to DEPT_MANAGER
//        notificationService.notifyDepartmentManagerForApproval(savedEmission);

        return emissionMapper.toResponse(savedEmission);
    }

    @Transactional
    public EmissionResponse approveEmission(Long emissionId) {

        EmissionData emissionData = emissionRepository.findById(emissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Emission not found with id: " + emissionId));

        User currentUser = authService.getCurrentUser();

        // Check permission based on role
        boolean canApprove = checkApprovalPermission(currentUser, emissionData);

        if (!canApprove) {
            throw new AccessDeniedException("You do not have permission to approve this emission data");
        }

        // Check status is PENDING
        if (emissionData.getStatus() != DataStatus.PENDING) {
            throw new BusinessException("Only PENDING records can be approved. Current status: "
                    + emissionData.getStatus());
        }

        // Approve the record
        emissionData.setStatus(DataStatus.APPROVED);
        emissionData.setApprovedBy(currentUser);
        emissionData.setApprovedAt(Instant.now());

        EmissionData savedEmission = emissionRepository.save(emissionData);


        // notificationService.notifySubmitterEmissionApproved(savedEmission);


        return emissionMapper.toResponse(savedEmission);
    }
    private boolean checkApprovalPermission(User approver, EmissionData emissionData) {

        Role role = approver.getRole();

        // DEPT_MANAGER: can only approve in his own department
        if (role == Role.DEPT_MANAGER) {
            return emissionData.getDepartment() != null &&
                    approver.getDepartment() != null &&
                    emissionData.getDepartment().getId().equals(approver.getDepartment().getId());
        }

        // SUSTAINABILITY_MANAGER: can approve any department in his company
        if (role == Role.SUSTAINABILITY_MANAGER) {
            return emissionData.getCompany() != null &&
                    approver.getCompany() != null &&
                    emissionData.getCompany().getId().equals(approver.getCompany().getId());
        }

        return false;
    }

    @Transactional
    public EmissionResponse rejectEmission(Long emissionId, String reason) {

        EmissionData emissionData = emissionRepository.findById(emissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Emission not found with id: " + emissionId));

        User currentUser = authService.getCurrentUser();

        if (!checkRejectPermission(currentUser, emissionData)) {
            throw new AccessDeniedException("You do not have permission to reject this emission data");
        }

        if (emissionData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING emissions can be rejected. Current status: "
                    + emissionData.getStatus());
        }

        emissionData.setStatus(DataStatus.REJECTED);
        emissionData.setRejectionReason(reason);

        emissionData = emissionRepository.save(emissionData);


//        sendRejectionNotification(emissionData);

        return emissionMapper.toResponse(emissionData);
    }
    private boolean checkRejectPermission(User approver, EmissionData emissionData) {
        if (approver == null || emissionData == null) {
            return false;
        }

        Role role = approver.getRole();

        // DEPT_MANAGER: can only reject in his own department
        if (role == Role.DEPT_MANAGER) {
            return emissionData.getDepartment() != null &&
                    approver.getDepartment() != null &&
                    emissionData.getDepartment().getId().equals(approver.getDepartment().getId());
        }

        // SUSTAINABILITY_MANAGER: can reject any department in his company
        if (role == Role.SUSTAINABILITY_MANAGER) {
            return emissionData.getCompany() != null &&
                    approver.getCompany() != null &&
                    emissionData.getCompany().getId().equals(approver.getCompany().getId());
        }

        return role == Role.ADMIN;
    }

    @Transactional
    public List<EmissionResponse> getEmissionByCompany(Long companyId) {

        User currentUser = authService.getCurrentUser();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        // Check if user belongs to the company or has global access
        boolean isAdminOrAuditor = currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.AUDITOR;

        boolean belongsToCompany = currentUser.getCompany() != null &&
                currentUser.getCompany().getId().equals(companyId);

        if (!belongsToCompany && !isAdminOrAuditor) {
            throw new AccessDeniedException("You do not have access to this company's emissions");
        }

        // Now filter based on role
        List<EmissionData> emissions;

        if (currentUser.getRole() == Role.EMPLOYEE) {
            //EMPLOYEE → only his own submissions
            emissions = emissionRepository.findAllBySubmittedBy(currentUser);

        } else if (currentUser.getRole() == Role.DEPT_MANAGER) {
            // DEPT_MANAGER -> only his department data
            if (currentUser.getDepartment() == null) {
                throw new BusinessException("Department manager has no assigned department");
            }
            emissions = emissionRepository.findAllByDepartment(currentUser.getDepartment());

        } else {
            // SUSTAINABILITY_MANAGER / ADMIN / AUDITOR → all company data
            emissions = emissionRepository.findAllByCompany_Id(company.getId());
        }

        // Convert to response and return
        return emissions.stream()
                .map(emissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmissionSummaryResponse getEmissionSummary(Long companyId, Instant startDate, Instant endDate) {

        // Validate dates
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new BadRequestException("Invalid date range: startDate must be before or equal to endDate");
        }

        // Verify company exists + permission check
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        User currentUser = authService.getCurrentUser();

        // Reuse similar permission logic as getEmissionByCompany
        if (!hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's emission summary");
        }

        // Get all APPROVED emissions in date range
        List<EmissionData> approvedEmissions = emissionRepository
                .findAllByCompanyAndStatusAndSubmittedAtBetween(
                        company, DataStatus.APPROVED, startDate, endDate);

        // Calculate totals
        BigDecimal totalCO2 = approvedEmissions.stream()
                .map(EmissionData::getCo2Amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCH4 = approvedEmissions.stream()
                .map(EmissionData::getCh4Amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalN2O = approvedEmissions.stream()
                .map(EmissionData::getN2oAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEmissions = totalCO2.add(totalCH4).add(totalN2O);

        EmissionSummaryResponse summary = new EmissionSummaryResponse();
        summary.setTotalCO2(totalCO2);
        summary.setTotalCH4(totalCH4);
        summary.setTotalN2O(totalN2O);
        summary.setTotalEmissions(totalEmissions);
        summary.setRecordCount(approvedEmissions.size());
        summary.setPeriod(startDate + " to " + endDate);

        return summary;
    }
    private boolean hasAccessToCompany(User user, Long companyId) {
        if (user == null) return false;

        // ADMIN / AUDITOR can see everything
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AUDITOR) {
            return true;
        }

        // Other roles must belong to the company
        return user.getCompany() != null &&
                user.getCompany().getId().equals(companyId);
    }

}