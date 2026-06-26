package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.SustainabilityTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SustainabilityTargetRepository extends JpaRepository<SustainabilityTarget, Long> {

    List<SustainabilityTarget> findByCompanyId(Long companyId);

    List<SustainabilityTarget> findByCompanyIdAndDepartmentId(Long companyId, Long departmentId);

    @Query("""
            SELECT t.targetValue
            FROM SustainabilityTarget t
            WHERE t.company.id = :companyId
            AND t.metricType = :metricType
            AND t.startDate <= :periodStart
            AND t.endDate >= :periodEnd
            ORDER BY t.startDate DESC
            LIMIT 1
            """)
    Optional<BigDecimal> findTargetValue(
            @Param("companyId") Long companyId,
            @Param("metricType") String metricType,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            SELECT t FROM SustainabilityTarget t
            WHERE t.company.id = :companyId
            AND t.category = :category
            AND t.startDate <= :date
            AND t.endDate >= :date
            """)
    List<SustainabilityTarget> findActiveTargetsByCategory(
            @Param("companyId") Long companyId,
            @Param("category") String category,
            @Param("date") LocalDate date
    );
}
