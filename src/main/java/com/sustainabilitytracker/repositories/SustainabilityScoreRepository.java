package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.SustainabilityScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SustainabilityScoreRepository extends JpaRepository<SustainabilityScore, Long> {

    List<SustainabilityScore> findByCompanyIdOrderByPeriodStartDesc(Long companyId);

    Optional<SustainabilityScore> findTopByCompanyIdOrderByCalculatedAtDesc(Long companyId);

    List<SustainabilityScore> findTop6ByCompanyIdOrderByPeriodStartDesc(Long companyId);

    Optional<SustainabilityScore> findByCompanyIdAndPeriodStartAndPeriodEnd(Long companyId, LocalDate periodStart, LocalDate periodEnd);

    @Query("""
            SELECT s FROM SustainabilityScore s
            WHERE s.calculatedAt = (
                SELECT MAX(s2.calculatedAt)
                FROM SustainabilityScore s2
                WHERE s2.company.id = s.company.id
                AND s2.company.isActive = true
            )
            ORDER BY s.totalScore DESC
            LIMIT 1
            """)
    Optional<SustainabilityScore> findBestLatestScore();

    @Query("""
            SELECT s FROM SustainabilityScore s
            WHERE s.calculatedAt = (
                SELECT MAX(s2.calculatedAt)
                FROM SustainabilityScore s2
                WHERE s2.company.id = s.company.id
                AND s2.company.isActive = true
            )
            ORDER BY s.totalScore ASC
            LIMIT 1
            """)
    Optional<SustainabilityScore> findWorstLatestScore();
}
