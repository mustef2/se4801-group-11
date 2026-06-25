package com.sustainabilitytracker.services;


import com.sustainabilitytracker.sustainabilitytracker.dtos.request.EmissionRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.EmissionResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.EmissionSummaryResponse;
import com.sustainabilitytracker.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.sustainabilitytracker.entities.EmissionData;
import com.sustainabilitytracker.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.sustainabilitytracker.mappers.EmissionMapper;
import com.sustainabilitytracker.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.EmissionRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmissionService {

    private final EmissionRepository emissionRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmissionMapper emissionMapper;
    private final AuthService authService;

    private static final BigDecimal CO2_THRESHOLD = new BigDecimal("10000");

    @Transactional
    public EmissionResponse submitEmission(EmissionRequest request) {

        Company company = companyRepository.findByIdAndIsActiveTrue(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found or inactive"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Department does not belong to the specified company");
        }

        User currentUser = authService.getCurrentUser();

        checkSubmissionPermission(currentUser, department, company);

        boolean alreadyApproved = emissionRepository
                .existsByDepartmentIdAndRecordedAtAndStatus(
                        department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new BusinessException("Data already submitted and approved for this date");
        }

        boolean hasWarning = isAbnormalValue(request.getCo2Amount());

        if (hasWarning) {
            log.warn("High CO2 value detected: {} kg for department: {}",
                    request.getCo2Amount(), department.getId());
        }

        EmissionData emissionData = emissionMapper.toEntity(request);
        emissionData.setCompany(company);
        emissionData.setDepartment(department);
        emissionData.setSubmittedBy(currentUser);
        emissionData.setStatus(DataStatus.DRAFT);

        EmissionData savedEmission = emissionRepository.save(emissionData);

        return emissionMapper.toResponse(savedEmission);
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

        EmissionData saved = emissionRepository.save(emissionData);

        return emissionMapper.toResponse(saved);
    }

    @Transactional
    public EmissionResponse approveEmission(Long emissionId) {

        EmissionData emissionData = emissionRepository.findById(emissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Emission not found with id: " + emissionId));

        User currentUser = authService.getCurrentUser();

        if (!canManageEmission(currentUser, emissionData)) {
            throw new AccessDeniedException("You do not have permission to approve this emission data");
        }

        if (emissionData.getStatus() != DataStatus.PENDING) {
            throw new BusinessException("Only PENDING records can be approved. Current status: "
                    + emissionData.getStatus());
        }

        emissionData.setStatus(DataStatus.APPROVED);
        emissionData.setApprovedBy(currentUser);
        emissionData.setApprovedAt(Instant.now());

        EmissionData saved = emissionRepository.save(emissionData);

        return emissionMapper.toResponse(saved);
    }

    @Transactional
    public EmissionResponse rejectEmission(Long emissionId, String reason) {

        EmissionData emissionData = emissionRepository.findById(emissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Emission not found with id: " + emissionId));

        User currentUser = authService.getCurrentUser();

        if (!canManageEmission(currentUser, emissionData)) {
            throw new AccessDeniedException("You do not have permission to reject this emission data");
        }

        if (StringUtils.isBlank(reason)) {
            throw new BadRequestException("Rejection reason is required");
        }

        if (reason.trim().length() < 10) {
            throw new BadRequestException("Rejection reason must be at least 10 characters");
        }

        if (emissionData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING emissions can be rejected. Current status: "
                    + emissionData.getStatus());
        }

        emissionData.setStatus(DataStatus.REJECTED);
        emissionData.setRejectionReason(reason.trim());

        EmissionData saved = emissionRepository.save(emissionData);

        return emissionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmissionResponse> getEmissionByCompany(Long companyId) {

        User currentUser = authService.getCurrentUser();

        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId));

        if (!hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's emissions");
        }

        List<EmissionData> emissions;

        if (currentUser.getRole() == Role.EMPLOYEE) {
            emissions = emissionRepository
                    .findBySubmittedById(currentUser.getId());

        } else if (currentUser.getRole() == Role.DEPT_MANAGER) {
            if (currentUser.getDepartment() == null) {
                throw new BusinessException("Department manager has no assigned department");
            }
            emissions = emissionRepository
                    .findByDepartmentId(currentUser.getDepartment().getId());

        } else {
            emissions = emissionRepository.findByCompanyId(companyId);
        }

        return emissions.stream()
                .map(emissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmissionSummaryResponse getEmissionSummary(
            Long companyId, LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BadRequestException(
                    "Invalid date range: startDate must be before or equal to endDate");
        }

        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId));

        User currentUser = authService.getCurrentUser();

        if (!hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException(
                    "You do not have access to this company's emission summary");
        }

        List<EmissionData> approvedEmissions = emissionRepository
                .findByCompanyIdAndStatusAndRecordedAtBetween(
                        companyId, DataStatus.APPROVED, startDate, endDate);

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
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AUDITOR) return true;
        return user.getCompany() != null && user.getCompany().getId().equals(companyId);
    }

    private void checkSubmissionPermission(
            User user, Department department, Company company) {

        if (user.getRole() == Role.EMPLOYEE || user.getRole() == Role.DEPT_MANAGER) {
            if (user.getDepartment() == null) {
                throw new BusinessException("Your account has no assigned department");
            }
            if (!user.getDepartment().getId().equals(department.getId())) {
                throw new AccessDeniedException("You can only submit for your own department");
            }
        }

        if (user.getRole() == Role.SUSTAINABILITY_MANAGER) {
            if (user.getCompany() == null ||
                    !user.getCompany().getId().equals(company.getId())) {
                throw new AccessDeniedException("You can only submit for your own company");
            }
        }
    }

    private boolean canManageEmission(User user, EmissionData emissionData) {
        if (user == null || emissionData == null) return false;

        if (user.getRole() == Role.ADMIN) return true;

        if (user.getRole() == Role.DEPT_MANAGER) {
            return emissionData.getDepartment() != null &&
                    user.getDepartment() != null &&
                    emissionData.getDepartment().getId()
                            .equals(user.getDepartment().getId());
        }

        if (user.getRole() == Role.SUSTAINABILITY_MANAGER) {
            return emissionData.getCompany() != null &&
                    user.getCompany() != null &&
                    emissionData.getCompany().getId()
                            .equals(user.getCompany().getId());
        }

        return false;
    }

    private boolean isAbnormalValue(BigDecimal co2Amount) {
        return co2Amount != null && co2Amount.compareTo(CO2_THRESHOLD) > 0;
    }
}