package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.responses.AuditResponse;
import com.sustainabilitytracker.entities.AuditRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditMapper {

    @Mapping(target = "reportId",    source = "report.id")
    @Mapping(target = "reportTitle", source = "report.reportTitle")
    @Mapping(target = "auditorId",   source = "auditor.id")
    @Mapping(target = "auditorName", source = "auditor.fullName")
    @Mapping(target = "createdAt",   source = "createdAt")
    AuditResponse toResponse(AuditRecord auditRecord);

    List<AuditResponse> toResponseList(List<AuditRecord> auditRecords);
}