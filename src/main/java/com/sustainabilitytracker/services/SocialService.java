package com.sustainabilitytracker.services;

import com.sustainabilitytracker.sustainabilitytracker.dtos.request.SocialRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.SocialResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.SocialSummaryResponse;
import com.sustainabilitytracker.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.sustainabilitytracker.entities.SocialData;
import com.sustainabilitytracker.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.sustainabilitytracker.mappers.SocialMapper;
import com.sustainabilitytracker.sustainabilitytracker.projection.SocialTotalsProjection;
import com.sustainabilitytracker.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.SocialRepository;
import com.sustainabilitytracker.sustainabilitytracker.utils.SecurityUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialService {

    private final SocialRepository socialRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SocialMapper socialMapper;

    @Transactional
    public SocialResponse submitSocial(SocialRequest request) {

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

        User currentUser = SecurityUtils.getCurrentUser();

        checkSubmitPermission(currentUser, department, company);

        if (request.getFemaleWorkers() != null && request.getTotalWorkers() != null
                && request.getFemaleWorkers() > request.getTotalWorkers()) {
            throw new BadRequestException("Female workers cannot exceed total workers");
        }

        if (request.getSatisfactionScore() != null && request.getSatisfactionScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Satisfaction score cannot exceed 100");
        }

        boolean alreadyApproved = socialRepository.existsByDepartmentIdAndRecordedAtAndStatus(
                department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new DuplicateResourceException("Social data already submitted and approved for this date");
        }

        SocialData socialData = socialMapper.toEntity(request);
        socialData.setCompany(company);
        socialData.setDepartment(department);
        socialData.setSubmittedBy(currentUser);
        socialData.setStatus(DataStatus.DRAFT);

        SocialData saved = socialRepository.save(socialData);

        log.info("Social data submitted for department: {} by user: {}",
                department.getId(), currentUser.getId());

        return socialMapper.toResponse(saved);
    }

    @Transactional
    public SocialResponse submitForApproval(Long socialId) {

        SocialData data = socialRepository.findById(socialId)
                .orElseThrow(() -> new ResourceNotFoundException("Social record not found with id: " + socialId));

        User currentUser = SecurityUtils.getCurrentUser();

        if (!data.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only submit your own records for approval");
        }

        if (data.getStatus() != DataStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT records can be submitted for approval");
        }

        data.setStatus(DataStatus.PENDING);
        data.setSubmittedAt(Instant.now());

        SocialData updated = socialRepository.save(data);

        return socialMapper.toResponse(updated);
    }

    @Transactional
    public SocialResponse approveSocial(Long socialId) {

        SocialData data = socialRepository.findById(socialId)
                .orElseThrow(() -> new ResourceNotFoundException("Social record not found with id: " + socialId));

        User approver = SecurityUtils.getCurrentUser();

        checkApprovePermission(approver, data);

        if (data.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be approved");
        }

        data.setStatus(DataStatus.APPROVED);
        data.setApprovedBy(approver);
        data.setApprovedAt(Instant.now());

        SocialData updated = socialRepository.save(data);

        return socialMapper.toResponse(updated);
    }

    @Transactional
    public SocialResponse rejectSocial(Long socialId, String reason) {

        SocialData data = socialRepository.findById(socialId)
                .orElseThrow(() -> new ResourceNotFoundException("Social record not found with id: " + socialId));

        User approver = SecurityUtils.getCurrentUser();

        checkApprovePermission(approver, data);

        if (StringUtils.isBlank(reason)) {
            throw new BadRequestException("Rejection reason is required");
        }

        if (data.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be rejected");
        }

        data.setStatus(DataStatus.REJECTED);
        data.setRejectionReason(reason.trim());

        SocialData updated = socialRepository.save(data);

        return socialMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<SocialResponse> getSocialByCompany(Long companyId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!SecurityUtils.hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's social data");
        }

        List<SocialData> list;

        if (currentUser.getRole() == Role.EMPLOYEE) {
            list = socialRepository.findBySubmittedById(currentUser.getId());
        } else if (currentUser.getRole() == Role.DEPT_MANAGER) {
            if (currentUser.getDepartment() == null) {
                throw new BusinessException("Department manager has no assigned department");
            }
            list = socialRepository.findByDepartmentId(currentUser.getDepartment().getId());
        } else {
            list = socialRepository.findByCompanyId(companyId);
        }

        return socialMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public SocialSummaryResponse getSocialSummary(Long companyId, LocalDate start, LocalDate end) {

        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        SocialTotalsProjection totals = socialRepository
                .getTotalsByCompanyAndPeriod(companyId, start, end);

        return SocialSummaryResponse.builder()
                .recordCount(totals.getRecordCount().intValue())
                .period(start + " to " + end)
                .build();
    }

    private void checkSubmitPermission(User user, Department department, Company company) {
        switch (user.getRole()) {
            case EMPLOYEE, DEPT_MANAGER -> {
                if (user.getDepartment() == null) {
                    throw new BadRequestException("Your account has no assigned department");
                }
                if (!user.getDepartment().getId().equals(department.getId())) {
                    throw new UnauthorizedException("You can only submit for your own department");
                }
            }
            case SUSTAINABILITY_MANAGER -> {
                if (user.getCompany() == null || !user.getCompany().getId().equals(company.getId())) {
                    throw new UnauthorizedException("You can only submit for your own company");
                }
            }
            default -> throw new UnauthorizedException("You do not have permission to submit social data");
        }
    }

    private void checkApprovePermission(User user, SocialData data) {
        switch (user.getRole()) {
            case DEPT_MANAGER -> {
                if (user.getDepartment() == null || data.getDepartment() == null ||
                        !user.getDepartment().getId().equals(data.getDepartment().getId())) {
                    throw new UnauthorizedException("You can only approve in your department");
                }
            }
            case SUSTAINABILITY_MANAGER -> {
                if (user.getCompany() == null || data.getCompany() == null ||
                        !user.getCompany().getId().equals(data.getCompany().getId())) {
                    throw new UnauthorizedException("You can only approve in your company");
                }
            }
            case ADMIN -> { /* Admin can approve anything */ }
            default -> throw new UnauthorizedException("You do not have permission to approve this record");
        }
    }
}