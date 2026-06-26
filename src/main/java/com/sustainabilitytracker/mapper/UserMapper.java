package com.sustainabilitytracker.mapper;

import com.sustainabilitytracker.dtos.requests.RegisterUserRequest;
import com.sustainabilitytracker.dtos.responses.UserResponse;
import com.sustainabilitytracker.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "company",      ignore = true)
    @Mapping(target = "department",   ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "isFirstLogin", ignore = true)
    @Mapping(target = "lastLogin",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    User toEntity(RegisterUserRequest request);

    @Mapping(source = "company.name",    target = "companyName")
    @Mapping(source = "department.name", target = "departmentName")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}