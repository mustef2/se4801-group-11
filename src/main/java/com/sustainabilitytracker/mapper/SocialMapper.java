package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.requests.SocialRequest;
import com.sustainabilitytracker.dtos.responses.SocialResponse;
import com.sustainabilitytracker.entities.SocialData;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SocialMapper {

    @Mapping(target = "company",         ignore = true)
    @Mapping(target = "department",      ignore = true)
    @Mapping(target = "submittedBy",     ignore = true)
    @Mapping(target = "approvedBy",      ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "submittedAt",     ignore = true)
    @Mapping(target = "approvedAt",      ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    SocialData toEntity(SocialRequest request);

    @Mapping(target = "companyId",       source = "company.id")
    @Mapping(target = "companyName",     source = "company.name")
    @Mapping(target = "departmentId",    source = "department.id")
    @Mapping(target = "departmentName",  source = "department.name")
    @Mapping(target = "submittedByName", source = "submittedBy.fullName")
    @Mapping(target = "approvedByName",  source = "approvedBy.fullName")
    SocialResponse toResponse(SocialData socialData);

    List<SocialResponse> toResponseList(List<SocialData> socialDataList);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget SocialData socialData,
                      SocialRequest request);
}