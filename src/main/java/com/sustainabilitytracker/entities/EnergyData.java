package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.EnergySource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "energy_data")
public class EnergyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "total_kwh")
    private BigDecimal totalKwh;

    @ColumnDefault("0.00")
    @Column(name = "renewable_kwh")
    private BigDecimal renewableKwh;

    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private EnergySource sourceType;

    @ColumnDefault("'DRAFT'")
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DataStatus status;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Lob
    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "recorded_at")
    private LocalDate recordedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;


}