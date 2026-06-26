package com.sustainabilitytracker.mapper;


import com.sustainabilitytracker.dtos.requests.CompanyRequest;
import com.sustainabilitytracker.dtos.responses.CompanyResponse;
import com.sustainabilitytracker.entities.Company;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    Company toEntity(CompanyRequest companyRequest);

    CompanyResponse toResponse(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isActive",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void update(CompanyRequest companyRequest,
                @MappingTarget Company company);
}
