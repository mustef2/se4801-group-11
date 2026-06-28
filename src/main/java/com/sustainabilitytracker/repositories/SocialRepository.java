package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.SocialData;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.projections.SocialTotalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SocialRepository extends JpaRepository<SocialData, Long> {

    boolean existsByDepartmentIdAndRecordedAtAndStatus(Long departmentId, LocalDate recordedAt, DataStatus status);

    List<SocialData> findByCompanyId(Long companyId);

    List<SocialData> findBySubmittedById(Long userId);

    List<SocialData> findByDepartmentId(Long departmentId);

    int countByCompanyIdAndStatus(Long companyId, DataStatus status);

    @Query("""
            SELECT
                COUNT(s.id)                                AS recordCount,
                COALESCE(SUM(s.totalWorkers), 0)           AS totalWorkers,
                COALESCE(SUM(s.femaleWorkers), 0)          AS totalFemaleWorkers,
                COALESCE(SUM(s.safetyIncidents), 0)        AS totalSafetyIncidents,
                COALESCE(AVG(s.trainingHours), 0)          AS avgTrainingHours,
                COALESCE(AVG(s.satisfactionScore), 0)      AS avgSatisfactionScore
            FROM SocialData s
            WHERE s.company.id = :companyId
            AND s.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            AND s.recordedAt BETWEEN :start AND :end
            """)
    SocialTotalsProjection getTotalsByCompanyAndPeriod(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(s.safetyIncidents), 0)
            FROM SocialData s
            WHERE s.company.id = :companyId
            AND s.recordedAt BETWEEN :start AND :end
            AND s.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    int getTotalSafetyIncidents(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(
                SUM(s.femaleWorkers) * 100.0 / NULLIF(SUM(s.totalWorkers), 0),
                0
            )
            FROM SocialData s
            WHERE s.company.id = :companyId
            AND s.recordedAt BETWEEN :start AND :end
            AND s.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    double getAverageFemaleRatio(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(AVG(s.trainingHours), 0)
            FROM SocialData s
            WHERE s.company.id = :companyId
            AND s.recordedAt BETWEEN :start AND :end
            AND s.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    BigDecimal getAverageTrainingHours(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT AVG(s.satisfactionScore)
            FROM SocialData s
            WHERE s.company.id = :companyId
            AND s.recordedAt BETWEEN :start AND :end
            AND s.status = com.sustainabilitytracker.enums.DataStatus.APPROVED
            """)
    BigDecimal getAverageSatisfactionScore(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}