package com.sustainabilitytracker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String email;
    private String fullName;
    private String role;
    private Long companyId;
    private Long departmentId;
    private boolean isFirstLogin;
}