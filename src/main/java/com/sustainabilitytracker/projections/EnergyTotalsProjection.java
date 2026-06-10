package com.sustainabilitytracker.projections;

import java.math.BigDecimal;

public interface EnergyTotalsProjection {
    BigDecimal getTotalKwh();
    BigDecimal getTotalRenewableKwh();
    BigDecimal getAverageKwh();
    Long getRecordCount();
}
