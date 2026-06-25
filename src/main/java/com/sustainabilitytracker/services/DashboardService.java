package com.sustainabilitytracker.services;

import com.sustainabilitytracker.dtos.responses.AdminDashboardResponse;
import com.sustainabilitytracker.dtos.responses.DashboardResponse;
import com.sustainabilitytracker.dtos.responses.ScoreResponse;
import com.sustainabilitytracker.dtos.responses.SystemStatsResponse;
import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.SustainabilityScore;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.AuditStatus;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.exceptions.AccessDeniedException;
import com.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.repositories.*;
import com.sustainabilitytracker.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SustainabilityScoreRepository scoreRepository;
    private final EmissionRepository emissionRepository;
    private final EnergyRepository energyRepository;
    private final WaterRepository waterRepository;
    private final WasteRepository wasteRepository;
    private final ReportRepository reportRepository;
    private final SocialRepository socialRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getCompanyDashboard(Long companyId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!SecurityUtils.hasAccessToCompany(currentUser, companyId)) {
            throw new AccessDeniedException(
                    "You do not have access to this company's dashboard"
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId
                ));

        ScoreResponse latestScore = mapToScoreResponse(
                scoreRepository
                        .findTopByCompanyIdOrderByCalculatedAtDesc(companyId)
                        .orElse(null)
        );

        List<ScoreResponse> scoreHistory = scoreRepository
                .findTop6ByCompanyIdOrderByPeriodStartDesc(companyId)
                .stream()
                .map(this::mapToScoreResponse)
                .collect(Collectors.toList());

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        BigDecimal totalCo2 = emissionRepository
                .getTotalCo2(companyId, monthStart, now);
        BigDecimal totalEnergy = energyRepository
                .getTotalKwh(companyId, monthStart, now);
        BigDecimal totalWater = waterRepository
                .getTotalConsumedLiters(companyId, monthStart, now);
        BigDecimal totalWaste = wasteRepository
                .getTotalKg(companyId, monthStart, now);

        int pendingApprovals = getPendingApprovalsCount(companyId);
        int pendingReports = (int) reportRepository
                .countByCompanyIdAndAuditStatus(
                        companyId, AuditStatus.PENDING
                );

        log.info("Dashboard loaded for company: {} by user: {}",
                companyId, currentUser.getId());

        return DashboardResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .latestScore(latestScore)
                .scoreHistory(scoreHistory)
                .totalCo2(totalCo2 != null
                        ? totalCo2 : BigDecimal.ZERO)
                .totalEnergyKwh(totalEnergy != null
                        ? totalEnergy : BigDecimal.ZERO)
                .totalWaterLiters(totalWater != null
                        ? totalWater : BigDecimal.ZERO)
                .totalWasteKg(totalWaste != null
                        ? totalWaste : BigDecimal.ZERO)
                .pendingApprovals(pendingApprovals)
                .pendingReports(pendingReports)
                .unreadNotifications(0)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {

        long totalCompanies = companyRepository.countByIsActiveTrue();
        long totalUsers = userRepository.countByIsActiveTrue();
        long totalReports = reportRepository.count();
        long pendingAudits = reportRepository
                .countByAuditStatus(AuditStatus.PENDING);

        ScoreResponse bestScore = mapToScoreResponse(
                scoreRepository.findBestLatestScore().orElse(null)
        );

        ScoreResponse worstScore = mapToScoreResponse(
                scoreRepository.findWorstLatestScore().orElse(null)
        );

        SystemStatsResponse stats = SystemStatsResponse.builder()
                .totalCompanies(totalCompanies)
                .totalUsers(totalUsers)
                .totalReports(totalReports)
                .pendingAudits(pendingAudits)
                .activeCompanies(totalCompanies)
                .build();

        log.info("Admin dashboard loaded");

        return AdminDashboardResponse.builder()
                .totalCompanies(totalCompanies)
                .totalUsers(totalUsers)
                .bestScore(bestScore)
                .worstScore(worstScore)
                .stats(stats)
                .build();
    }

    private int getPendingApprovalsCount(Long companyId) {
        int emission = emissionRepository
                .countByCompanyIdAndStatus(companyId, DataStatus.PENDING);
        int energy = energyRepository
                .countByCompanyIdAndStatus(companyId, DataStatus.PENDING);
        int water = waterRepository
                .countByCompanyIdAndStatus(companyId, DataStatus.PENDING);
        int waste = wasteRepository
                .countByCompanyIdAndStatus(companyId, DataStatus.PENDING);
        int social = socialRepository
                .countByCompanyIdAndStatus(companyId, DataStatus.PENDING);

        return emission + energy + water + waste + social;
    }

    private ScoreResponse mapToScoreResponse(SustainabilityScore entity) {
        if (entity == null) return null;

        return ScoreResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompany().getId())
                .companyName(entity.getCompany().getName())
                .environmentScore(entity.getEnvironmentScore())
                .socialScore(entity.getSocialScore())
                .governanceScore(entity.getGovernanceScore())
                .totalScore(entity.getTotalScore())
                .grade(entity.getGrade())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .calculatedAt(entity.getCalculatedAt())
                .build();
    }
}