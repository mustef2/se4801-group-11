package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.EnergyData;
import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.projections.EnergyTotalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EnergyRepository extends JpaRepository<EnergyData, Long> {

    boolean existsByDepartmentIdAndRecordedAtAndStatus(Long departmentId, LocalDate recordedAt, DataStatus status);

    List<EnergyData> findByCompanyId(Long companyId);

    List<EnergyData> findByDepartmentId(Long departmentId);

    List<EnergyData> findBySubmittedById(Long userId);

    int countByCompanyIdAndStatus(Long companyId, DataStatus status);

    @Query("""
            SELECT
                SUM(e.totalKwh)     AS totalKwh,
                SUM(e.renewableKwh) AS totalRenewableKwh,
                AVG(e.totalKwh)     AS averageKwh,
                COUNT(e.id)         AS recordCount
            FROM EnergyData e
            WHERE e.company.id = :companyId
            AND e.recordedAt BETWEEN :start AND :end
            AND e.status = 'APPROVED'
            """)
    EnergyTotalsProjection getTotalsByCompanyAndPeriod(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(e.totalKwh), 0)
            FROM EnergyData e
            WHERE e.company.id = :companyId
            AND e.recordedAt BETWEEN :start AND :end
            AND e.status = 'APPROVED'
            """)
    BigDecimal getTotalKwh(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(e.renewableKwh), 0)
            FROM EnergyData e
            WHERE e.company.id = :companyId
            AND e.recordedAt BETWEEN :start AND :end
            AND e.status = 'APPROVED'
            """)
    BigDecimal getTotalRenewableKwh(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}