package com.sustainabilitytracker.projections;

import java.math.BigDecimal;

public interface GovernanceTotalsProjection {
    Long getRecordCount();
    BigDecimal getAverageComplianceScore();
    Integer getTotalPolicies();
    Integer getTotalViolations();
    BigDecimal getAverageBoardDiversity();
}
