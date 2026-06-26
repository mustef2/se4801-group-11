package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.requests.WasteRequest;
import com.sustainabilitytracker.dtos.responses.WasteResponse;
import com.sustainabilitytracker.dtos.responses.WasteSummaryResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.entities.WasteData;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.mapper.WasteMapper;
import com.sustainabilitytracker.projections.WasteTotalsProjection;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.repositories.WasteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WasteService {

    private final WasteRepository wasteRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final WasteMapper wasteMapper;
    private final AuthService authService;
//    private final NotificationService notificationService;
//    private final AuditLogService auditLogService;
//    private final ScoreService scoreService;

    @Transactional
    public WasteResponse submitWaste(WasteRequest request) {

        Long currentUserId = authService.getCurrentUser().getId();

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        if (!company.getIsActive()) {
            throw new BadRequestException("Company is not active");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (!department.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Department does not belong to this company");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        checkSubmitPermission(currentUser, department, company);

        // Check duplicate approved record
        boolean alreadyApproved = wasteRepository
                .existsByDepartmentIdAndRecordedAtAndStatus(
                        department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new DuplicateResourceException("Waste data already submitted and approved for this date");
        }

        WasteData wasteData = wasteMapper.toEntity(request);
        wasteData.setCompany(company);
        wasteData.setDepartment(department);
        wasteData.setSubmittedBy(currentUser);
        wasteData.setStatus(DataStatus.DRAFT);

        WasteData saved = wasteRepository.save(wasteData);

//        notificationService.notifyByDepartmentAndRole(
//                department.getId(), "DEPT_MANAGER", "New waste data submitted for: " + department.getName());
//
//        auditLogService.log("SUBMIT_WASTE", "WASTE", saved.getId(), null, saved);

        return wasteMapper.toResponse(saved);
    }

    @Transactional
    public WasteResponse submitForApproval(Long wasteId) {
        Long currentUserId = authService.getCurrentUser().getId();

        WasteData wasteData = wasteRepository.findById(wasteId)
                .orElseThrow(() -> new ResourceNotFoundException("Waste record not found with id: " + wasteId));

        if (!wasteData.getSubmittedBy().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only submit your own records for approval");
        }

        if (wasteData.getStatus() != DataStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT records can be submitted for approval");
        }

        wasteData.setStatus(DataStatus.PENDING);
        wasteData.setSubmittedAt(Instant.now());

        WasteData updated = wasteRepository.save(wasteData);

//        notificationService.notifyByDepartmentAndRole(
//                wasteData.getDepartment().getId(), "DEPT_MANAGER", "Waste record waiting for approval");

        return wasteMapper.toResponse(updated);
    }

    @Transactional
    public WasteResponse approveWaste(Long wasteId) {
        Long currentUserId = authService.getCurrentUser().getId();

        WasteData wasteData = wasteRepository.findById(wasteId)
                .orElseThrow(() -> new ResourceNotFoundException("Waste record not found with id: " + wasteId));

        User approver = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        checkApprovePermission(approver, wasteData);

        if (wasteData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be approved");
        }

        wasteData.setStatus(DataStatus.APPROVED);
        wasteData.setApprovedBy(approver);
        wasteData.setApprovedAt(Instant.now());

        WasteData updated = wasteRepository.save(wasteData);

//        scoreService.recalculateForCompany(updated.getCompany().getId());
//
//        notificationService.notifyUser(updated.getSubmittedBy().getId(), "Your waste record has been APPROVED");

        return wasteMapper.toResponse(updated);
    }

    @Transactional
    public WasteResponse rejectWaste(Long wasteId, String reason) {
        Long currentUserId = authService.getCurrentUser().getId();

        WasteData wasteData = wasteRepository.findById(wasteId)
                .orElseThrow(() -> new ResourceNotFoundException("Waste record not found with id: " + wasteId));

        User approver = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        checkApprovePermission(approver, wasteData);

        if (wasteData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be rejected");
        }

        wasteData.setStatus(DataStatus.REJECTED);
        wasteData.setRejectionReason(reason);

        WasteData updated = wasteRepository.save(wasteData);

//        notificationService.notifyUser(updated.getSubmittedBy().getId(),
//                "Your waste record has been REJECTED. Reason: " + reason);

        return wasteMapper.toResponse(updated);
    }

    public List<WasteResponse> getWasteByCompany(Long companyId) {

        Long currentUserId = authService.getCurrentUser().getId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (!hasCompanyAccess(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's waste data");
        }

        List<WasteData> wasteList;

        if (currentUser.getRole() == Role.EMPLOYEE) {
            wasteList = wasteRepository.findBySubmittedById(currentUserId);
        } else if (currentUser.getRole() == Role.DEPT_MANAGER) {
            wasteList = wasteRepository.findByDepartmentId(currentUser.getDepartment().getId());
        } else {
            wasteList = wasteRepository.findByCompanyId(companyId);
        }

        return wasteMapper.toResponseList(wasteList);
    }

    public WasteSummaryResponse getWasteSummary(Long companyId, LocalDate start, LocalDate end) {

        WasteTotalsProjection totals = wasteRepository
                .getTotalsByCompanyAndPeriod(companyId, start, end);

        return WasteSummaryResponse.builder()
                .totalKg(totals.getTotalKg())
                .totalRecycledKg(totals.getTotalRecycledKg())
                .totalHazardousKg(totals.getTotalHazardousKg())
                .recyclingRate(totals.getRecyclingRate())
                .recordCount(totals.getRecordCount().intValue())
                .period(start + " to " + end)
                .build();
    }

    private void checkSubmitPermission(User user, Department department, Company company) {
        switch (user.getRole()) {
            case EMPLOYEE:
            case DEPT_MANAGER:
                if (!user.getDepartment().getId().equals(department.getId())) {
                    throw new UnauthorizedException("You can only submit for your own department");
                }
                break;
            case SUSTAINABILITY_MANAGER:
                if (!user.getCompany().getId().equals(company.getId())) {
                    throw new UnauthorizedException("You can only submit for your own company");
                }
                break;
            default:
                throw new UnauthorizedException("You do not have permission to submit waste data");
        }
    }

    private void checkApprovePermission(User user, WasteData wasteData) {
        switch (user.getRole()) {
            case DEPT_MANAGER:
                if (!user.getDepartment().getId().equals(wasteData.getDepartment().getId())) {
                    throw new UnauthorizedException("You can only approve in your department");
                }
                break;
            case SUSTAINABILITY_MANAGER:
                if (!user.getCompany().getId().equals(wasteData.getCompany().getId())) {
                    throw new UnauthorizedException("You can only approve in your company");
                }
                break;
            case ADMIN:
                return;
            default:
                throw new UnauthorizedException("You do not have permission to approve this record");
        }
    }

    private boolean hasCompanyAccess(User user, Long companyId) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AUDITOR) {
            return true;
        }
        return user.getCompany() != null && user.getCompany().getId().equals(companyId);
    }
}
