package com.sustainabilitytracker.projections;

import java.math.BigDecimal;

public interface EmissionTotalsProjection {
    BigDecimal getTotalCO2();
    BigDecimal getTotalCH4();
    BigDecimal getTotalN2O();
    Long getRecordCount();
}
