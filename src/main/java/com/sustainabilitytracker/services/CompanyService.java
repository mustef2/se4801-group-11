package com.sustainabilitytracker.services;


import com.sustainabilitytracker.dtos.requests.CompanyRequest;
import com.sustainabilitytracker.dtos.responses.CompanyResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.exceptions.DuplicateResourceException;
import com.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.mapper.CompanyMapper;
import com.sustainabilitytracker.repositories.CompanyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyResponse createCompany(CompanyRequest request){
        if (companyRepository.existsByName(request.getName()))
            throw new DuplicateResourceException(request.getName() + " is already exist.");

        var company = companyMapper.toEntity(request);
        company.setIsActive(true);
        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAllByIsActive(true)
                .stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    public CompanyResponse getCompanyById(Long companyId){
        return companyRepository
                .findById(companyId)
                .map(companyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }

    public CompanyResponse updateCompany(Long companyId, CompanyRequest request) {
        Company company = companyRepository
                .findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found with id: " + companyId));

        if (StringUtils.hasText(request.getName()) &&
                !request.getName().equals(company.getName())) {

            if (companyRepository.existsByNameAndIdNot(request.getName(), companyId)) {
                throw new DuplicateResourceException("Company name already exists: " + request.getName());
            }
            company.setName(request.getName());
        }

        companyMapper.update(request, company);

        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    @Transactional
    public void deactivateCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with id: " + companyId
                        )
                );

        company.setIsActive(false);

        company.getUsers().forEach(user -> user.setIsActive(false));

    }
}