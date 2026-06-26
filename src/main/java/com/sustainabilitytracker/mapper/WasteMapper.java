package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.requests.WasteRequest;
import com.sustainabilitytracker.dtos.responses.WasteResponse;
import com.sustainabilitytracker.entities.WasteData;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WasteMapper {

    @Mapping(target = "company",         ignore = true)
    @Mapping(target = "department",      ignore = true)
    @Mapping(target = "submittedBy",     ignore = true)
    @Mapping(target = "approvedBy",      ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "submittedAt",     ignore = true)
    @Mapping(target = "approvedAt",      ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    WasteData toEntity(WasteRequest request);

    @Mapping(target = "companyId",       source = "company.id")
    @Mapping(target = "companyName",     source = "company.name")
    @Mapping(target = "departmentId",    source = "department.id")
    @Mapping(target = "departmentName",  source = "department.name")
    @Mapping(target = "submittedByName", source = "submittedBy.fullName")
    @Mapping(target = "approvedByName",  source = "approvedBy.fullName")
    WasteResponse toResponse(WasteData wasteData);

    List<WasteResponse> toResponseList(List<WasteData> wasteDataList);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget WasteData wasteData,
                      WasteRequest request);
}