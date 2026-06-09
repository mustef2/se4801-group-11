package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.requests.CompanyRequest;
import com.sustainabilitytracker.dtos.responses.CompanyResponse;
import com.sustainabilitytracker.entities.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company toEntity(CompanyRequest companyRequest);

    CompanyResponse toResponse(Company company);
    void update(CompanyRequest companyRequest, @MappingTarget Company company);
}
