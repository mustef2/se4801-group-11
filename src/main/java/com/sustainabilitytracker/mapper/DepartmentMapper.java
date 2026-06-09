package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.requests.DepartmentRequest;
import com.sustainabilitytracker.dtos.responses.DepartmentResponse;
import com.sustainabilitytracker.entities.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    Department toEntity(DepartmentRequest request);

    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "createdAt", source = "createdAt")
    DepartmentResponse toResponse(Department department);
}