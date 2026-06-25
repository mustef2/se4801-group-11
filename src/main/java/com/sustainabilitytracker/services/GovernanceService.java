package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.requests.GovernanceRequest;
import com.sustainabilitytracker.dtos.responses.GovernanceResponse;
import com.sustainabilitytracker.dtos.responses.GovernanceSummaryResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.GovernanceData;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.mapper.GovernanceMapper;
import com.sustainabilitytracker.projections.GovernanceTotalsProjection;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.GovernanceRepository;
import com.sustainabilitytracker.utils.SecurityUtils;
import io.micrometer.common.util.StringUtils;
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
public class GovernanceService {

    private final GovernanceRepository governanceRepository;
    private final CompanyRepository companyRepository;
    private final GovernanceMapper governanceMapper;

    @Transactional
    public GovernanceResponse submitGovernance(GovernanceRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        if (!company.getIsActive()) {
            throw new BadRequestException("Company is not active");
        }

        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getRole() != Role.SUSTAINABILITY_MANAGER) {
            throw new AccessDeniedException("Only Sustainability Manager can submit governance data");
        }

        boolean alreadyApproved = governanceRepository
                .existsByCompanyIdAndRecordedAtAndStatus(
                        company.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new DuplicateResourceException("Governance data already submitted and approved for this date");
        }

        GovernanceData data = governanceMapper.toEntity(request);
        data.setCompany(company);
        data.setSubmittedBy(currentUser);
        data.setStatus(DataStatus.DRAFT);

        GovernanceData saved = governanceRepository.save(data);

        log.info("Governance data submitted for company: {} by user: {}",
                company.getId(), currentUser.getId());

        return governanceMapper.toResponse(saved);
    }

    @Transactional
    public GovernanceResponse submitForApproval(Long governanceId) {

        GovernanceData data = governanceRepository.findById(governanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Governance record not found with id: " + governanceId));

        User currentUser = SecurityUtils.getCurrentUser();

        if (!data.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only submit your own records for approval");
        }

        if (data.getStatus() != DataStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT records can be submitted for approval");
        }

        data.setStatus(DataStatus.PENDING);
        data.setSubmittedAt(Instant.now());

        GovernanceData updated = governanceRepository.save(data);

        return governanceMapper.toResponse(updated);
    }

    @Transactional
    public GovernanceResponse approveGovernance(Long governanceId) {

        GovernanceData data = governanceRepository.findById(governanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Governance record not found with id: " + governanceId));

        User approver = SecurityUtils.getCurrentUser();

        checkApprovePermission(approver, data);

        if (data.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be approved");
        }

        data.setStatus(DataStatus.APPROVED);
        data.setApprovedBy(approver);
        data.setApprovedAt(Instant.now());

        GovernanceData updated = governanceRepository.save(data);

        return governanceMapper.toResponse(updated);
    }

    @Transactional
    public GovernanceResponse rejectGovernance(Long governanceId, String reason) {

        GovernanceData data = governanceRepository.findById(governanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Governance record not found with id: " + governanceId));

        User approver = SecurityUtils.getCurrentUser();

        checkApprovePermission(approver, data);

        if (StringUtils.isBlank(reason)) {
            throw new BadRequestException("Rejection reason is required");
        }
        if (reason.trim().length() < 10) {
            throw new BadRequestException("Rejection reason must be at least 10 characters long");
        }

        if (data.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be rejected");
        }

        data.setStatus(DataStatus.REJECTED);
        data.setRejectionReason(reason.trim());

        GovernanceData updated = governanceRepository.save(data);

        return governanceMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<GovernanceResponse> getGovernanceByCompany(Long companyId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!SecurityUtils.hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's governance data");
        }

        // Only Sustainability Manager and above should view governance data
        if (currentUser.getRole() == Role.EMPLOYEE || currentUser.getRole() == Role.DEPT_MANAGER) {
            throw new AccessDeniedException("You do not have permission to view governance data");
        }

        List<GovernanceData> list = governanceRepository.findByCompanyId(companyId);

        return governanceMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public GovernanceSummaryResponse getGovernanceSummary(Long companyId, LocalDate start, LocalDate end) {

        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        GovernanceTotalsProjection totals = governanceRepository
                .getTotalsByCompanyAndPeriod(companyId, start, end);

        return GovernanceSummaryResponse.builder()
                .recordCount(totals.getRecordCount().intValue())
                .period(start + " to " + end)
                .build();
    }

    private void checkApprovePermission(User user, GovernanceData data) {
        switch (user.getRole()) {
            case SUSTAINABILITY_MANAGER, ADMIN -> {
                return;
            }
            default -> throw new UnauthorizedException("You do not have permission to approve this record");
        }
    }
}