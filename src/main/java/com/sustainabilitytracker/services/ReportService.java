package com.sustainabilitytracker.services;

import com.sustainabilitytracker.config.ReportProperties;
import com.sustainabilitytracker.dtos.requests.ReportRequest;
import com.sustainabilitytracker.dtos.responses.ReportResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.EsgReport;
import com.sustainabilitytracker.entities.SustainabilityScore;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.*;
import com.sustainabilitytracker.exceptions.AccessDeniedException;
import com.sustainabilitytracker.exceptions.BadRequestException;
import com.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.mapper.ReportMapper;
import com.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final CompanyRepository companyRepository;
    private final AuthService authService;
    private final ScoreCalculationService scoreCalculationService;
    private final ReportMapper reportMapper;
    private final ReportProperties reportProperties;

    @Transactional
    public ReportResponse generateReport(ReportRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        User currentUser = authService.getCurrentUser();

        if (!hasAccessToCompany(currentUser, company.getId())) {
            throw new AccessDeniedException("You do not have access to generate report for this company");
        }

        LocalDate start = request.getPeriodStart();
        LocalDate end = request.getPeriodEnd();

        validatePeriod(start, end);

        // Calculate latest score
        SustainabilityScore score = scoreCalculationService
                .calculateAndSaveScore(company.getId(), start, end, PeriodType.MONTHLY);

        // Generate file
        String fileName = generateFileName(company, start, end, request.getFileFormat());
        String filePath = reportProperties.getStoragePath() + fileName;

        byte[] fileBytes = generateReportFile(company, start, end, request.getFileFormat());
        saveFileToStorage(fileBytes, filePath);

        // Save report record
        EsgReport report = EsgReport.builder()
                .company(company)
                .score(score)
                .generatedBy(currentUser)
                .reportTitle(request.getReportTitle() != null ? request.getReportTitle() : "ESG Sustainability Report")
                .reportType(request.getReportType() != null ? request.getReportType() : ReportType.FULL_ESG)
                .filePath(filePath)
                .fileFormat(request.getFileFormat() != null ? request.getFileFormat().name() : "PDF")
                .periodStart(start)
                .periodEnd(end)
                .auditStatus(AuditStatus.PENDING)
                .build();

        EsgReport savedReport = reportRepository.save(report);

        return reportMapper.toResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByCompany(Long companyId) {

        User currentUser = authService.getCurrentUser();

        if (!hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException("You do not have access to this company's reports");
        }

        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        List<EsgReport> reports = reportRepository.findByCompanyIdOrderByGeneratedAtDesc(companyId);

        return reportMapper.toResponseList(reports);
    }

    @Transactional(readOnly = true)
    public byte[] downloadReport(Long reportId) {

        EsgReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User currentUser = authService.getCurrentUser();

        if (!hasAccessToCompany(currentUser, report.getCompany().getId())) {
            throw new AccessDeniedException("You do not have access to this report");
        }

        return readFileFromStorage(report.getFilePath());
    }

    // PRIVATE HELPERS

    private boolean hasAccessToCompany(User user, Long companyId) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AUDITOR) return true;
        return user.getCompany() != null && user.getCompany().getId().equals(companyId);
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BadRequestException("Period start and end dates are required");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("Period end date cannot be before start date");
        }
        if (start.isAfter(LocalDate.now())) {
            throw new BadRequestException("Cannot generate report for future period");
        }
    }

    private String generateFileName(Company company, LocalDate start, LocalDate end, FileFormat format) {
        String ext = format != null ? format.name().toLowerCase() : "pdf";
        return company.getName().replace(" ", "_") +
                "_ESG_Report_" + start + "_to_" + end + "." + ext;
    }

    private byte[] generateReportFile(Company company, LocalDate start, LocalDate end, FileFormat format) {
        // TODO: Implement real PDF/Excel generation later (iText or Apache POI)
        if (format == FileFormat.EXCEL) {
            throw new UnsupportedOperationException("Excel report generation not implemented yet");
        }
        // Default to plain text for now
        String content = "ESG Sustainability Report\n" +
                "Company: " + company.getName() + "\n" +
                "Period: " + start + " to " + end + "\n" +
                "Generated At: " + Instant.now();
        return content.getBytes();
    }

    private void saveFileToStorage(byte[] bytes, String filePath) {
        try {
            Path path = Path.of(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            log.info("Report saved successfully: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save report file: {}", filePath, e);
            throw new RuntimeException("Failed to save report file", e);
        }
    }

    private byte[] readFileFromStorage(String filePath) {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            log.error("Failed to read report file: {}", filePath, e);
            throw new RuntimeException("Failed to read report file", e);
        }
    }

    public ReportResponse getReportById(Long reportId) {
        return reportMapper.toResponse(reportRepository.getEsgReportById(reportId));
    }
}