package com.sustainabilitytracker.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository {

    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM emission_data WHERE company_id = :companyId AND status = 'PENDING') +
            (SELECT COUNT(*) FROM energy_data WHERE company_id = :companyId AND status = 'PENDING') +
            (SELECT COUNT(*) FROM water_data WHERE company_id = :companyId AND status = 'PENDING') +
            (SELECT COUNT(*) FROM waste_data WHERE company_id = :companyId AND status = 'PENDING') +
            (SELECT COUNT(*) FROM social_data WHERE company_id = :companyId AND status = 'PENDING')
        AS total_pending
        """, nativeQuery = true)
    int countAllPendingByCompanyId(@Param("companyId") Long companyId);
}