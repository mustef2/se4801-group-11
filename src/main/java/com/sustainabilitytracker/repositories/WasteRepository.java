package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.sustainabilitytracker.entities.WasteData;
import com.sustainabilitytracker.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.sustainabilitytracker.projection.WasteTotalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WasteRepository extends JpaRepository<WasteData, Long> {

    boolean existsByDepartmentIdAndRecordedAtAndStatus(Long departmentId, LocalDate recordedAt, DataStatus status);

    List<WasteData> findByCompanyId(Long companyId);

    List<WasteData> findByDepartmentId(Long departmentId);

    List<WasteData> findBySubmittedById(Long userId);

    int countByCompanyIdAndStatus(Long companyId, DataStatus status);

    @Query("""
            SELECT
                SUM(w.totalKg)     AS totalKg,
                SUM(w.recycledKg)  AS totalRecycledKg,
                SUM(w.hazardousKg) AS totalHazardousKg,
                COUNT(w.id)        AS recordCount,
                COALESCE(SUM(w.recycledKg) * 100.0
                    / NULLIF(SUM(w.totalKg), 0), 0) AS recyclingRate
            FROM WasteData w
            WHERE w.company.id = :companyId
            AND w.status = 'APPROVED'
            AND w.recordedAt BETWEEN :start AND :end
            """)
    WasteTotalsProjection getTotalsByCompanyAndPeriod(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(w.totalKg), 0)
            FROM WasteData w
            WHERE w.company.id = :companyId
            AND w.recordedAt BETWEEN :start AND :end
            AND w.status = 'APPROVED'
            """)
    BigDecimal getTotalKg(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COALESCE(SUM(w.recycledKg), 0)
            FROM WasteData w
            WHERE w.company.id = :companyId
            AND w.recordedAt BETWEEN :start AND :end
            AND w.status = 'APPROVED'
            """)
    BigDecimal getTotalRecycledKg(
            @Param("companyId") Long companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}