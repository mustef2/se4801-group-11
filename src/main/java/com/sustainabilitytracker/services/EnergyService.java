package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.requests.EnergyRequest;
import com.sustainabilitytracker.dtos.responses.EnergyResponse;
import com.sustainabilitytracker.dtos.responses.EnergySummaryResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.entities.EnergyData;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.*;
import com.sustainabilitytracker.mapper.EnergyMapper;
import com.sustainabilitytracker.projections.EnergyTotalsProjection;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.repositories.EnergyRepository;
import com.sustainabilitytracker.repositories.UserRepository;
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
public class EnergyService {

    private final EnergyRepository energyRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EnergyMapper energyMapper;
//    private final NotificationService notificationService;
//    private final AuditLogService auditLogService;
//    private final ScoreService scoreService;

    private static final BigDecimal KWH_THRESHOLD = new BigDecimal("100000");
    private final AuthService authService;


    @Transactional
    public EnergyResponse submitEnergy(EnergyRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));
        User user = authService.getCurrentUser();
        if (!company.getIsActive()) {
            throw new BadRequestException("Company is not active");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (!department.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Department does not belong to this company");
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getId()));

        checkSubmitPermission(currentUser, department, company);

        // Check for duplicate approved record
        boolean alreadyApproved = energyRepository
                .existsByDepartmentIdAndRecordedAtAndStatus(
                        department.getId(), request.getRecordedAt(), DataStatus.APPROVED);

        if (alreadyApproved) {
            throw new DuplicateResourceException("Energy data already submitted and approved for this date");
        }

        // Handle abnormal KWH value
        String notes = request.getNotes();
        if (request.getTotalKwh() != null && request.getTotalKwh().compareTo(KWH_THRESHOLD) > 0) {
            log.warn("Abnormal KWH detected: {} for company: {}", request.getTotalKwh(), company.getId());
            notes = "[WARNING: Abnormal KWH value] " + (notes != null ? notes : "");
        }

        EnergyData energyData = energyMapper.toEntity(request);
        energyData.setCompany(company);
        energyData.setDepartment(department);
        energyData.setSubmittedBy(currentUser);
        energyData.setStatus(DataStatus.DRAFT);
        energyData.setNotes(notes);

        EnergyData savedEnergy = energyRepository.save(energyData);

        // Notify department manager
//        notificationService.notifyByDepartmentAndRole(
//                department.getId(), "DEPT_MANAGER", "New energy data submitted for: " + department.getName());

        // Audit log
//        auditLogService.log("SUBMIT_ENERGY", "ENERGY", savedEnergy.getId(), null, savedEnergy);

        return energyMapper.toResponse(savedEnergy);
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
                throw new UnauthorizedException("You do not have permission to submit energy data");
        }
    }


    @Transactional
    public EnergyResponse submitForApproval(Long energyId) {

        EnergyData energyData = energyRepository.findById(energyId)
                .orElseThrow(() -> new ResourceNotFoundException("Energy record not found with id: " + energyId));
        User currentUser = authService.getCurrentUser();
        if (!energyData.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only submit your own records for approval");
        }

        if (energyData.getStatus() != DataStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT records can be submitted for approval. Current: " + energyData.getStatus());
        }

        energyData.setStatus(DataStatus.PENDING);
        energyData.setSubmittedAt(Instant.now());

        EnergyData updated = energyRepository.save(energyData);

//        notificationService.notifyByDepartmentAndRole(
//                energyData.getDepartment().getId(),
//                "DEPT_MANAGER",
//                "Energy record waiting for approval");

        return energyMapper.toResponse(updated);
    }


    @Transactional
    public EnergyResponse approveEnergy(Long energyId) {
        User currentUser = authService.getCurrentUser();

        EnergyData energyData = energyRepository.findById(energyId)
                .orElseThrow(() -> new ResourceNotFoundException("Energy record not found with id: " + energyId));

        User approver = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        checkApprovePermission(approver, energyData);

        if (energyData.getStatus() != DataStatus.PENDING) {
            throw new BadRequestException("Only PENDING records can be approved");
        }

        energyData.setStatus(DataStatus.APPROVED);
        energyData.setApprovedBy(approver);
        energyData.setApprovedAt(Instant.now());

        EnergyData updated = energyRepository.save(energyData);

//        scoreService.recalculateForCompany(updated.getCompany().getId());
//
//        notificationService.notifyUser(
//                updated.getSubmittedBy().getId(), "Your energy record has been APPROVED");
//
//        auditLogService.log("APPROVE_ENERGY", "ENERGY", updated.getId(), null, updated);

        return energyMapper.toResponse(updated);
    }



    private void checkApprovePermission(User user, EnergyData energyData) {
        switch (user.getRole()) {
            case DEPT_MANAGER:
                if (!user.getDepartment().getId().equals(energyData.getDepartment().getId())) {
                    throw new UnauthorizedException("You can only approve data in your department");
                }
                break;
            case SUSTAINABILITY_MANAGER:
                if (!user.getCompany().getId().equals(energyData.getCompany().getId())) {
                    throw new UnauthorizedException("You can only approve data in your company");
                }
                break;
            case ADMIN:
                return; // Admin can approve anything
            default:
                throw new UnauthorizedException("You do not have permission to approve this record");
        }
    }

}
