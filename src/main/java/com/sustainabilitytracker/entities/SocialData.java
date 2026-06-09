package com.sustainabilitytracker.entities;

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
@Table(name = "social_data")
public class SocialData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    @Column(name = "total_workers")
    private Integer totalWorkers;

    @Column(name = "female_workers")
    private Integer femaleWorkers;

    @ColumnDefault("0")
    @Column(name = "safety_incidents")
    private Integer safetyIncidents;

    @ColumnDefault("0.00")
    @Column(name = "training_hours")
    private BigDecimal trainingHours;

    @Column(name = "satisfaction_score")
    private BigDecimal satisfactionScore;

    @ColumnDefault("'DRAFT'")
    @Column(name = "status")
    private String status;

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