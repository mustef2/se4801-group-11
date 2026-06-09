package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.UserDto;
import com.sustainabilitytracker.dtos.requests.RegisterUserRequest;
import com.sustainabilitytracker.dtos.responses.UserResponse;
import com.sustainabilitytracker.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "department", ignore = true)
    User toEntity(RegisterUserRequest request);

    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "department.name", target = "departmentName")
    UserResponse toResponse(User user);
}