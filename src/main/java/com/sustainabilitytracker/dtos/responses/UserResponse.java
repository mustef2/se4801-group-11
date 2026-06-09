package com.sustainabilitytracker.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.Instant;

@Data
@JsonPropertyOrder({
        "id",
        "fullName",
        "email",
        "role",
        "companyName",
        "departmentName",
        "isActive",
        "createdAt"
})
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String companyName;
    private String departmentName;
    private Boolean isActive;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
}
