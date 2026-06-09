package com.sustainabilitytracker.services;

import com.sustainabilitytracker.mapper.DepartmentMapper;
import com.sustainabilitytracker.dtos.requests.DepartmentRequest;
import com.sustainabilitytracker.dtos.responses.DepartmentResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.exceptions.DuplicateResourceException;
import com.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.DepartmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
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

        return departmentMapper.toResponse(saved);
    }

    @Transactional
    public List<DepartmentResponse> getDepartmentsByCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with id: " + companyId
                        )
                );

        return company.getDepartments()
                .stream()
                .filter(department -> Boolean.TRUE.equals(department.getIsActive()))
                .map(departmentMapper::toResponse)
                .toList();
    }


    @Transactional
    public DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest request) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + departmentId
                        )
                );

        Long companyId = request.getCompanyId();

        //  Verify it belongs to same company
        if (!department.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException(
                    "Department does not belong to company with id: " + companyId
            );
        }

        // Check duplicate name in same company (excluding current department)
        if (request.getName() != null &&
                departmentRepository.existsByNameAndCompanyIdAndIdNot(
                        request.getName(),
                        companyId,
                        departmentId
                )) {

            throw new DuplicateResourceException(
                    "Department already exists with name: " + request.getName()
            );
        }

        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        return departmentMapper.toResponse(department);
    }

    @Transactional
    public void deactivateDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + departmentId
                        )
                );

        department.setIsActive(false);

        department.getUsers()
                .forEach(user -> user.setIsActive(false));

    }
}
