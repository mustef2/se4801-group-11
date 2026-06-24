package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.requests.WaterRequest;
import com.sustainabilitytracker.dtos.responses.WaterResponse;
import com.sustainabilitytracker.dtos.responses.WaterSummaryResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.entities.WaterData;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.mapper.WaterMapper;
import com.sustainabilitytracker.projections.WaterTotalsProjection;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.repositories.WaterRepository;
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
public class WaterService {
    private final WaterRepository waterRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthService authService;
    private final WaterMapper waterMapper;
//    private final NotificationService notificationService;
//    private final AuditLogService auditLogService;
//    private final ScoreService scoreService;

    //SUBMIT WATER
    @Transactional
    public WaterResponse submitWater(WaterRequest request) {

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

        User currentUser = authService.getCurrentUser();

        checkSubmitPermission(currentUser, department, company);

        boolean alreadyApproved = waterRepository
                .existsByDepartmentIdAndRecordedAtAndStatus(
                        department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new DuplicateResourceException("Water data already submitted and approved for this date");
        }

        WaterData waterData = waterMapper.toEntity(request);
        waterData.setCompany(company);
        waterData.setDepartment(department);
        waterData.setSubmittedBy(currentUser);
        waterData.setStatus(DataStatus.DRAFT);

        WaterData saved = waterRepository.save(waterData);

//        notificationService.notifyByDepartmentAndRole(
//                department.getId(), "DEPT_MANAGER", "New water data submitted for: " + department.getName());
//
//        auditLogService.log("SUBMIT_WATER", "WATER", saved.getId(), null, saved);

        return waterMapper.toResponse(saved);
    }

    // SUBMIT FOR APPROVAL
    @Transactional
    public WaterResponse submitForApproval(Long waterId) {
        WaterData waterData = waterRepository.findById(waterId)
                .orElseThrow(() -> new ResourceNotFoundException("Water record not found with id: " + waterId));

        User currentUser = authService.getCurrentUser();

        if (!waterData.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only submit your own records for approval");
        }

        if (waterData.getStatus() != DataStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT records can be submitted for approval");
        }

        waterData.setStatus(DataStatus.PENDING);
        waterData.setSubmittedAt(Instant.now());

        WaterData updated = waterRepository.save(waterData);

//        notificationService.notifyByDepartmentAndRole(
//                waterData.getDepartment().getId(), "DEPT_MANAGER", "Water record waiting for approval");

        return waterMapper.toResponse(updated);
    }

    // APPROVE WATER
    @Transactional
    public WaterResponse approveWater(Long waterId) {
        WaterData waterData = waterRepository.findById(waterId)
                .orElseThrow(() -> new ResourceNotFoundException("Water record not found with id: " + waterId));

        User approver = authService.getCurrentUser();

        checkApprovePermission(approver, waterData);

        if (waterData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be approved");
        }

        waterData.setStatus(DataStatus.APPROVED);
        waterData.setApprovedBy(approver);
        waterData.setApprovedAt(Instant.now());

        WaterData updated = waterRepository.save(waterData);

//        scoreService.recalculateForCompany(updated.getCompany().getId());
//
//        notificationService.notifyUser(updated.getSubmittedBy().getId(), "Your water record has been APPROVED");

        return waterMapper.toResponse(updated);
    }

    // REJECT WATER
    @Transactional
    public WaterResponse rejectWater(Long waterId, String reason) {
        WaterData waterData = waterRepository.findById(waterId)
                .orElseThrow(() -> new ResourceNotFoundException("Water record not found with id: " + waterId));

        User approver = authService.getCurrentUser();

        checkApprovePermission(approver, waterData);

        if (waterData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be rejected");
        }

        waterData.setStatus(DataStatus.REJECTED);
        waterData.setRejectionReason(reason);

        WaterData updated = waterRepository.save(waterData);

//        notificationService.notifyUser(updated.getSubmittedBy().getId(),
//                "Your water record has been REJECTED. Reason: " + reason);

        return waterMapper.toResponse(updated);
    }

    // GET WATER BY COMPANY
    public List<WaterResponse> getWaterByCompany(Long companyId) {
        User currentUser = authService.getCurrentUser();

        if (!hasCompanyAccess(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's water data");
        }

        List<WaterData> waterList;

        if (currentUser.getRole() == Role.EMPLOYEE) {
            waterList = waterRepository.findBySubmittedById(currentUser.getId());
        } else if (currentUser.getRole() == Role.DEPT_MANAGER) {
            waterList = waterRepository.findByDepartmentId(currentUser.getDepartment().getId());
        } else {
            waterList = waterRepository.findByCompanyId(companyId);
        }

        return waterMapper.toResponseList(waterList);
    }

    //GET WATER SUMMARY
    public WaterSummaryResponse getWaterSummary(Long companyId, LocalDate start, LocalDate end) {
        WaterTotalsProjection totals = waterRepository
                .getTotalsByCompanyAndPeriod(companyId, start, end);

        return WaterSummaryResponse.builder()
                .totalConsumedLiters(totals.getTotalConsumedLiters())
                .totalRecycledLiters(totals.getTotalRecycledLiters())
                .recyclingRate(totals.getRecyclingRate())
                .recordCount(totals.getRecordCount().intValue())
                .period(start + " to " + end)
                .build();
    }

    // PRIVATE HELPERS
    private void checkSubmitPermission(User user, Department department, Company company) {
        switch (user.getRole()) {
            case EMPLOYEE, DEPT_MANAGER:
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
                throw new UnauthorizedException("You do not have permission to submit water data");
        }
    }

    private void checkApprovePermission(User user, WaterData waterData) {
        switch (user.getRole()) {
            case DEPT_MANAGER:
                if (!user.getDepartment().getId().equals(waterData.getDepartment().getId())) {
                    throw new UnauthorizedException("You can only approve in your department");
                }
                break;
            case SUSTAINABILITY_MANAGER:
                if (!user.getCompany().getId().equals(waterData.getCompany().getId())) {
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