package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.CompanySize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String industry;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank
    private CompanySize size;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

}
