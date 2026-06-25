package com.sustainabilitytracker.services;

import com.sustainabilitytracker.sustainabilitytracker.dtos.request.DepartmentRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.DepartmentResponse;
import com.sustainabilitytracker.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.BadRequestException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.DuplicateResourceException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.sustainabilitytracker.mappers.DepartmentMapper;
import com.sustainabilitytracker.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final CompanyRepository companyRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        Company company = companyRepository
                .findByIdAndIsActiveTrue(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active company not found with id: " + request.getCompanyId()));

        if (departmentRepository.existsByNameAndCompanyId(request.getName(), company.getId())) {
            throw new DuplicateResourceException(
                    "Department already exists with name: " + request.getName());
        }

        Department department = departmentMapper.toEntity(request);
        department.setCompany(company);
        department.setIsActive(true);

        Department saved = departmentRepository.save(department);

        log.info("Department '{}' created under company: {}",
                saved.getName(), company.getName());

        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId));

        return company.getDepartments()
                .stream()
                .filter(department -> Boolean.TRUE.equals(department.getIsActive()))
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest request) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + departmentId));

        Long companyId = request.getCompanyId();

        if (!department.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(
                    "Department does not belong to company with id: " + companyId);
        }

        // Check duplicate name in the same company (excluding current department)
        if (StringUtils.hasText(request.getName()) &&
                departmentRepository.existsByNameAndCompanyIdAndIdNot(
                        request.getName(), companyId, departmentId)) {

            throw new DuplicateResourceException(
                    "Department already exists with name: " + request.getName());
        }

        // Update fields
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        Department saved = departmentRepository.save(department);

        log.info("Department {} updated successfully", departmentId);

        return departmentMapper.toResponse(saved);
    }

    @Transactional
    public void deactivateDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + departmentId));

        if (!department.getIsActive()) {
            throw new BadRequestException("Department is already deactivated");
        }

        department.setIsActive(false);

        // Deactivate all users in this department
        department.getUsers().forEach(user -> user.setIsActive(false));

        departmentRepository.save(department);

        log.info("Department {} ('{}') deactivated. {} users also deactivated.",
                departmentId, department.getName(), department.getUsers().size());
    }
}