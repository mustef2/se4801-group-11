package com.sustainabilitytracker.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private String companyName;
    private Boolean isActive;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy, HH:mm", timezone = "UTC")
    private Instant createdAt;
}
