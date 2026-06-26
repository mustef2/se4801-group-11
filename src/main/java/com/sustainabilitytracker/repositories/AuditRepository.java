package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.AuditRecord;
import com.sustainabilitytracker.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, Long> {

    List<AuditRecord> findByReportIdOrderByCreatedAtDesc(Long reportId);

    List<AuditRecord> findByAuditorId(Long auditorId);

    List<AuditRecord> findByCompanyId(Long companyId);

    int countByReportIdAndAction(Long reportId, AuditAction action);

    boolean existsByReportIdAndAuditorId(Long reportId, Long auditorId);
}