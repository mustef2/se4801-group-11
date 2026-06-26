package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.DataStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Getter
@Setter
@Entity
@Table(name = "governance_data")
public class GovernanceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "compliance_score")
    private BigDecimal complianceScore;

    @ColumnDefault("0")
    @Column(name = "policy_count")
    private Integer policyCount;

    @ColumnDefault("0")
    @Column(name = "violations_count")
    private Integer violationsCount;

    @Column(name = "board_diversity_pct")
    private BigDecimal boardDiversityPct;

    @ColumnDefault("0")
    @Column(name = "ethics_training_done")
    private Boolean ethicsTrainingDone;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}