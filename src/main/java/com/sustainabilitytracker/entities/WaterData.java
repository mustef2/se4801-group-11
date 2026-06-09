package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.DataStatus;
import com.sustainabilitytracker.enums.WaterSource;
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
@Table(name = "water_data")
public class WaterData {
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

    @Column(name = "consumed_liters")
    private BigDecimal consumedLiters;

    @ColumnDefault("0.00")
    @Column(name = "recycled_liters")
    private BigDecimal recycledLiters;

    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    private WaterSource source;

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