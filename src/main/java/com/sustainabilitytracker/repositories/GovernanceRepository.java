package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.GovernanceData;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.projections.GovernanceTotalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GovernanceRepository extends JpaRepository<GovernanceData, Long> {

    boolean existsByCompanyIdAndRecordedAtAndStatus(Long companyId, LocalDate recordedAt, DataStatus status);

    List<GovernanceData> findByCompanyId(Long companyId);

    int countByCompanyIdAndStatus(Long companyId, DataStatus status);

    @Query("""
            SELECT
                COUNT(g.id)              AS recordCount,
                AVG(g.complianceScore)   AS averageComplianceScore,
                SUM(g.policyCount)       AS totalPolicies,
                SUM(g.violationsCount)   AS totalViolations,
                AVG(g.boardDiversityPct) AS averageBoardDiversity
            FROM GovernanceData g
            WHERE g.company.id = :companyId
            AND g.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            AND g.recordedAt BETWEEN :start AND :end
            """)
    GovernanceTotalsProjection getTotalsByCompanyAndPeriod(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT AVG(g.complianceScore)
            FROM GovernanceData g
            WHERE g.company.id = :companyId
            AND g.recordedAt BETWEEN :start AND :end
            AND g.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    BigDecimal getAverageComplianceScore(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(g.violationsCount), 0)
            FROM GovernanceData g
            WHERE g.company.id = :companyId
            AND g.recordedAt BETWEEN :start AND :end
            AND g.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    int getTotalViolations(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COUNT(g) > 0
            FROM GovernanceData g
            WHERE g.company.id = :companyId
            AND g.recordedAt BETWEEN :start AND :end
            AND g.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            AND g.ethicsTrainingDone = true
            """)
    boolean hasEthicsTraining(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}