package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

//@ValidRegisterUser
@Data
public class RegisterUserRequest {

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @Email
    @NotBlank(message = "Email is required.")
//    @Lowercase(message = "Email must be lowercase.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private Long companyId;
    private Long departmentId;
}