package com.sustainabilitytracker.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String industry;
    private String country;
    private String city;
    private String size;
    private String email;
    private String phone;
    private Boolean isActive;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy, HH:mm", timezone = "UTC")
    private Instant createdAt;

}