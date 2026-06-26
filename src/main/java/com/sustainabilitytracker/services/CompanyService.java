package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.requests.CompanyRequest;
import com.sustainabilitytracker.dtos.responses.CompanyResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.exceptions.BadRequestException;
import com.sustainabilitytracker.exceptions.DuplicateResourceException;
import com.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.mapper.CompanyMapper;
import com.sustainabilitytracker.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {

        if (companyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(request.getName() + " is already exist.");
        }

        Company company = companyMapper.toEntity(request);
        company.setIsActive(true);

        Company savedCompany = companyRepository.save(company);

        log.info("Company created successfully: {} (ID: {})", savedCompany.getName(), savedCompany.getId());

        return companyMapper.toResponse(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAllByIsActiveTrue()
                .stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long companyId, CompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        // Handle name uniqueness check before mapper update
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(company.getName())) {
            if (companyRepository.existsByNameAndIdNot(request.getName(), companyId)) {
                throw new DuplicateResourceException("Company name already exists: " + request.getName());
            }
        }

        // Let mapper update all fields (including name if changed)
        companyMapper.update(request, company);

        Company savedCompany = companyRepository.save(company);

        log.info("Company updated: {} (ID: {})", savedCompany.getName(), savedCompany.getId());

        return companyMapper.toResponse(savedCompany);
    }

    @Transactional
    public void deactivateCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        if (!company.getIsActive()) {
            throw new BadRequestException("Company is already deactivated");
        }

        company.setIsActive(false);

        // Deactivate all users in the company
        company.getUsers().forEach(user -> user.setIsActive(false));

        companyRepository.save(company);

        log.info("Company {} ('{}') deactivated successfully. {} users also deactivated.",
                companyId, company.getName(), company.getUsers().size());
    }
}