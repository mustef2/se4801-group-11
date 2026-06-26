package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.EsgReport;
import com.sustainabilitytracker.enums.AuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<EsgReport, Long> {

    List<EsgReport> findByCompanyIdOrderByGeneratedAtDesc(Long companyId);

    Optional<EsgReport> findByIdAndCompanyId(Long id, Long companyId);

    List<EsgReport> findByAuditStatus(AuditStatus status);

    int countByCompanyIdAndAuditStatus(Long companyId, AuditStatus auditStatus);

    long countByAuditStatus(AuditStatus auditStatus);

    EsgReport getEsgReportById(Long reportId);
}