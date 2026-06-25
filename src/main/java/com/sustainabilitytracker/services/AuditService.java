package com.sustainabilitytracker.services;

import com.sustainabilitytracker.sustainabilitytracker.dtos.request.AuditRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.AuditResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.ReportResponse;
import com.sustainabilitytracker.sustainabilitytracker.entities.AuditRecord;
import com.sustainabilitytracker.sustainabilitytracker.entities.EsgReport;
import com.sustainabilitytracker.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.sustainabilitytracker.enums.AuditAction;
import com.sustainabilitytracker.sustainabilitytracker.enums.AuditStatus;
import com.sustainabilitytracker.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.AccessDeniedException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.BusinessException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.sustainabilitytracker.mappers.AuditMapper;
import com.sustainabilitytracker.sustainabilitytracker.mappers.ReportMapper;
import com.sustainabilitytracker.sustainabilitytracker.repositories.AuditRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final ReportRepository reportRepository;
    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;
    private final ReportMapper reportMapper;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsForAudit() {
        List<EsgReport> reports = reportRepository.findByAuditStatus(AuditStatus.PENDING);

        return reportMapper.toResponseList(reports);
    }

    @Transactional
    public AuditResponse reviewReport(Long reportId, AuditRequest request) {

        EsgReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User auditor = authService.getCurrentUser();

        // Prevent reviewing already finalized reports
        if (report.getAuditStatus() == AuditStatus.VERIFIED ||
                report.getAuditStatus() == AuditStatus.REJECTED) {
            throw new BusinessException("This report has already been finalized with status: "
                    + report.getAuditStatus());
        }

        // Create audit record
        AuditRecord auditRecord = AuditRecord.builder()
                .report(report)
                .auditor(auditor)
                .company(report.getCompany())
                .action(request.getAction())
                .comments(request.getComments())
                .flaggedItems(request.getFlaggedItems())
                .build();

        AuditRecord savedAudit = auditRepository.save(auditRecord);

        // Update report status
        AuditStatus newStatus = getUpdatedAuditStatus(request.getAction());
        report.setAuditStatus(newStatus);
        reportRepository.save(report);

        log.info("Report {} reviewed by auditor {}. New status: {}",
                reportId, auditor.getId(), newStatus);

        // TODO: Uncomment when NotificationService is ready
        // notificationService.notifyUser(...);

        return auditMapper.toResponse(savedAudit);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditHistory(Long reportId) {

        EsgReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User currentUser = authService.getCurrentUser();

        if (!hasAccessToReport(currentUser, report)) {
            throw new AccessDeniedException("You do not have access to this report's audit history");
        }

        List<AuditRecord> audits = auditRepository.findByReportIdOrderByCreatedAtDesc(reportId);

        return auditMapper.toResponseList(audits);
    }

    private AuditStatus getUpdatedAuditStatus(AuditAction action) {
        return switch (action) {
            case VERIFIED -> AuditStatus.VERIFIED;
            case FLAGGED -> AuditStatus.FLAGGED;
            case REJECTED -> AuditStatus.REJECTED;
            case REQUESTED_INFO -> AuditStatus.UNDER_REVIEW;
        };
    }

    private boolean hasAccessToReport(User user, EsgReport report) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AUDITOR) {
            return true;
        }

        // Only Sustainability Manager of the company can see audit history
        if (user.getRole() == Role.SUSTAINABILITY_MANAGER) {
            return user.getCompany() != null &&
                    user.getCompany().getId().equals(report.getCompany().getId());
        }

        return false;
    }
}