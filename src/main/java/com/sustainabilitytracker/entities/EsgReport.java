package com.sustainabilitytracker.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "esg_reports")
public class EsgReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id")
    private SustainabilityScore score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "report_title")
    private String reportTitle;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_format")
    private String fileFormat;

    @ColumnDefault("'PENDING'")
    @Column(name = "audit_status")
    private String auditStatus;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "generated_at")
    private Instant generatedAt;


}