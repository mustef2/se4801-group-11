package com.sustainabilitytracker.mapper;


import com.sustainabilitytracker.dtos.requests.EmissionRequest;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.entities.EmissionData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
//    @Mapping(target = "hasWarning", source = "hasWarning")
    EmissionData toEntity(EmissionRequest request);

    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "submittedByName", source = "submittedBy.fullName")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    EmissionResponse toResponse(EmissionData emissionData);

    void updateEntity(@MappingTarget EmissionData emissionData, EmissionRequest request);
}