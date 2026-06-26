package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.responses.ReportResponse;
import com.sustainabilitytracker.entities.EsgReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "companyId",    source = "company.id")
    @Mapping(target = "companyName",  source = "company.name")
    @Mapping(target = "scoreId",      source = "score.id")
    @Mapping(target = "auditStatus",  source = "auditStatus")
    @Mapping(target = "downloadUrl",  ignore = true)
    ReportResponse toResponse(EsgReport report);

    List<ReportResponse> toResponseList(List<EsgReport> reports);
}