package com.sustainabilitytracker.dtos.requests;

import com.sustainabilitytracker.enums.ReportType;
import com.sustainabilitytracker.enums.FileFormat;
import com.sustainabilitytracker.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Period start is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;

    private ReportType reportType;

    @NotNull(message = "File format is required")
    private FileFormat fileFormat;

    private String reportTitle;
}